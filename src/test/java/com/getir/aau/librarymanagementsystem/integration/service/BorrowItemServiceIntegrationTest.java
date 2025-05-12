package com.getir.aau.librarymanagementsystem.integration.service;

import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.*;
import com.getir.aau.librarymanagementsystem.repository.BookRepository;
import com.getir.aau.librarymanagementsystem.repository.BorrowItemRepository;
import com.getir.aau.librarymanagementsystem.repository.BorrowRecordRepository;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.service.BorrowItemService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("BorrowItemService Integration Tests")
class BorrowItemServiceIntegrationTest {

    @Autowired private BorrowItemService borrowItemService;
    @Autowired private BorrowItemRepository borrowItemRepository;
    @Autowired private BorrowRecordRepository borrowRecordRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private UserRepository userRepository;

    private User regularUser;
    private User librarian;
    private Book book;
    private BorrowItem borrowItem;

    @BeforeEach
    void setUp() {
        Role userRole = new Role(1L, ERole.ROLE_USER, "USER");
        Role librarianRole = new Role(2L, ERole.ROLE_LIBRARIAN, "LIBRARIAN");

        regularUser = userRepository.save(User.builder()
                .firstName("Test")
                .lastName("User")
                .email("user@example.com")
                .password("pw")
                .phoneNumber("1234567890")
                .role(userRole)
                .build());

        librarian = userRepository.save(User.builder()
                .firstName("Lib")
                .lastName("Admin")
                .email("lib@example.com")
                .password("pw")
                .phoneNumber("0000000000")
                .role(librarianRole)
                .build());

        book = bookRepository.save(Book.builder()
                .title("Integration Book")
                .isbn("999-1234567890")
                .description("Test Book Desc")
                .publicationDate(LocalDate.of(2023, 1, 1))
                .genre("Test")
                .numberOfCopies(2)
                .available(true)
                .author(Author.builder().id(1L).name("Author Name").description("Author desc").build())
                .category(Category.builder().id(1L).name("Test Category").build())
                .build());

        BorrowRecord borrowRecord = borrowRecordRepository.save(BorrowRecord.builder()
                .user(regularUser)
                .borrowDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().plusDays(4))
                .build());

        borrowItem = borrowItemRepository.save(BorrowItem.builder()
                .book(book)
                .user(regularUser)
                .borrowRecord(borrowRecord)
                .borrowDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().minusDays(1))
                .returned(false)
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        regularUser.getEmail(),
                        regularUser.getPassword(),
                        List.of(() -> "ROLE_USER")
                )
        );
    }

    @Nested
    @DisplayName("returnBook integration")
    class ReturnBookIntegrationTests {

        @Test
        @DisplayName("Should return book successfully")
        void returnBook_asUser_success() {
            borrowItemService.returnBook(borrowItem.getId(), regularUser.getId());

            BorrowItem updatedItem = borrowItemRepository.findById(borrowItem.getId()).orElseThrow();
            Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();

            assertTrue(updatedItem.isReturned());
            assertEquals(3, updatedBook.getNumberOfCopies());
            assertTrue(updatedBook.isAvailable());
        }

        @Test
        @DisplayName("Should return book successfully as librarian")
        void returnBook_asLibrarian_success() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            librarian.getEmail(),
                            librarian.getPassword(),
                            List.of(() -> "ROLE_LIBRARIAN")
                    )
            );

            borrowItemService.returnBook(borrowItem.getId(), regularUser.getId());

            BorrowItem updatedItem = borrowItemRepository.findById(borrowItem.getId()).orElseThrow();
            Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();

            assertTrue(updatedItem.isReturned());
            assertEquals(3, updatedBook.getNumberOfCopies());
            assertTrue(updatedBook.isAvailable());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when non-owner non-librarian attempts return")
        void returnBook_unauthorized_throwsException() {
            User differentUser = userRepository.save(User.builder()
                    .firstName("Other")
                    .lastName("User")
                    .email("other@example.com")
                    .password("pw")
                    .phoneNumber("9999999999")
                    .role(new Role(1L, ERole.ROLE_USER, "USER"))
                    .build());
            
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            differentUser.getEmail(),
                            differentUser.getPassword(),
                            List.of(() -> "ROLE_USER")
                    )
            );
            
            assertThrows(AccessDeniedException.class, () ->
                borrowItemService.returnBook(borrowItem.getId(), differentUser.getId())
            );
        }

        @Test
        @DisplayName("Should throw IllegalStateException when returning already returned book")
        void returnBook_alreadyReturned_throwsException() {
            // First return
            borrowItemService.returnBook(borrowItem.getId(), regularUser.getId());
            
            // Second return attempt
            assertThrows(IllegalStateException.class, () ->
                borrowItemService.returnBook(borrowItem.getId(), regularUser.getId())
            );
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent borrow item")
        void returnBook_nonExistentItem_throwsException() {
            assertThrows(ResourceNotFoundException.class, () ->
                borrowItemService.returnBook(999L, regularUser.getId())
            );
        }

    }

    @Nested
    @DisplayName("getByUserId integration")
    class GetByUserIdIntegrationTests {

        @Test
        @DisplayName("Should retrieve borrow items by user ID")
        void getByUserId_success() {
            Pageable pageable = PageRequest.of(0, 10);

            BorrowItemPageResponseDto response = borrowItemService.getByUserId(regularUser.getId(), pageable);

            assertEquals(1, response.items().size());
            assertEquals(1, response.totalPages());
            assertEquals(1, response.totalItems());
            assertEquals(borrowItem.getId(), response.items().getFirst().id());
        }
    }
    @Nested
    @DisplayName("BorrowItemService extra integration")
    class AdditionalIntegrationTests {

        @Test
        @DisplayName("Should retrieve items by book ID")
        void getByBookId_success() {
            Pageable pageable = PageRequest.of(0, 10);
            BorrowItemPageResponseDto response = borrowItemService.getByBookId(book.getId(), pageable);
            assertEquals(1, response.items().size());
        }

        @Test
        @DisplayName("Should retrieve overdue items")
        void getOverdueItems_success() {
            Pageable pageable = PageRequest.of(0, 10);
            BorrowItemPageResponseDto response = borrowItemService.getOverdueItems(pageable);
            assertEquals(1, response.items().size());
        }

        @Test
        @DisplayName("Should retrieve items by date range")
        void getByDateRange_success() {
            Pageable pageable = PageRequest.of(0, 10);
            LocalDate start = LocalDate.now().minusDays(30);
            LocalDate end = LocalDate.now();
            BorrowItemPageResponseDto response = borrowItemService.getBorrowItemsByDateRange(start, end, pageable);
            assertEquals(1, response.items().size());
        }

        @Test
        @DisplayName("Should return active items by user")
        void getActiveItemsByUser_success() {
            List<BorrowItemResponseDto> activeItems = borrowItemService.getActiveItemsByUser(regularUser.getId());
            assertEquals(1, activeItems.size());
        }

        @Test
        @DisplayName("Should detect overdue items for user")
        void existsOverdueItems_success() {
            boolean hasOverdue = borrowItemService.existsOverdueItemsByUserId(regularUser.getId());
            assertTrue(hasOverdue);
        }

        @Test
        @DisplayName("Should count active borrow items")
        void countActiveItems_success() {
            int count = borrowItemService.countActiveItemsByUser(regularUser.getId());
            assertEquals(1, count);
        }

        @Test
        @DisplayName("Should throw exception for invalid date range")
        void getByDateRange_invalidRange_throwsException() {
            Pageable pageable = PageRequest.of(0, 10);
            LocalDate start = LocalDate.now();
            LocalDate end = LocalDate.now().minusDays(1);

            assertThrows(IllegalArgumentException.class, () ->
                    borrowItemService.getBorrowItemsByDateRange(start, end, pageable)
            );
        }

        @Test
        @DisplayName("Should handle empty pages correctly")
        void getByUserId_emptyPage_success() {
            Pageable pageable = PageRequest.of(1, 10);

            BorrowItemPageResponseDto response = borrowItemService.getByUserId(regularUser.getId(), pageable);

            assertTrue(response.items().isEmpty());
            assertEquals(1, response.totalPages());
            assertEquals(1, response.totalItems());
        }
    }
}