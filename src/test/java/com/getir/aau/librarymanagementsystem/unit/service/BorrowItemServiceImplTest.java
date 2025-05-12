package com.getir.aau.librarymanagementsystem.unit.service;

import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.*;
import com.getir.aau.librarymanagementsystem.model.mapper.BorrowMapper;
import com.getir.aau.librarymanagementsystem.repository.BookRepository;
import com.getir.aau.librarymanagementsystem.repository.BorrowItemRepository;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.security.SecurityUtils;
import com.getir.aau.librarymanagementsystem.service.impl.BorrowItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowItemServiceImplTest {

    @Mock private BorrowItemRepository borrowItemRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private BorrowMapper borrowMapper;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks private BorrowItemServiceImpl borrowItemService;

    private User librarian;
    private User regularUser;
    private BorrowItem borrowItem;
    private Book book;

    @BeforeEach
    void setUp() {
        regularUser = User.builder()
                .id(2L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .password("encodedPassword")
                .phoneNumber("555-1234")
                .role(Role.builder().id(1L).name(ERole.ROLE_USER).description("ROLE_USER").build())
                .build();

        librarian = User.builder()
                .id(1L)
                .firstName("Librarian")
                .lastName("Admin")
                .email("lib@example.com")
                .password("encoded")
                .phoneNumber("555-0000")
                .role(Role.builder().id(2L).name(ERole.ROLE_LIBRARIAN).description("ROLE_LIBRARIAN").build())
                .build();

        book = Book.builder()
                .id(100L)
                .title("Test Book")
                .isbn("123-4567890123")
                .description("Test Description")
                .publicationDate(LocalDate.of(2020, 1, 1))
                .genre("Science Fiction")
                .numberOfCopies(1)
                .available(true)
                .author(Author.builder().id(1L).name("Author Name").description("Author desc").build())
                .category(Category.builder().id(1L).name("Test Category").build())
                .build();

        borrowItem = BorrowItem.builder()
                .id(10L)
                .user(regularUser)
                .book(book)
                .borrowDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(9))
                .returned(false)
                .build();
    }

    @Nested
    @DisplayName("returnBook()")
    class ReturnBookTests {

        @Test
        @DisplayName("Should return book successfully if user is librarian")
        void returnBookByLibrarian() {
            // Arrange
            when(borrowItemRepository.findById(10L)).thenReturn(Optional.of(borrowItem));
            when(securityUtils.getCurrentUser()).thenReturn(librarian);
            when(securityUtils.hasRole("LIBRARIAN")).thenReturn(true);

            // Act
            borrowItemService.returnBook(10L, regularUser.getId());

            // Assert
            assertTrue(borrowItem.isReturned());
            verify(bookRepository).save(book);
            verify(borrowItemRepository).save(borrowItem);
        }

        @Test
        @DisplayName("Should return book successfully if user is owner")
        void returnBookByOwner() {
            when(borrowItemRepository.findById(10L)).thenReturn(Optional.of(borrowItem));
            when(securityUtils.getCurrentUser()).thenReturn(regularUser);
            when(securityUtils.hasRole("LIBRARIAN")).thenReturn(false);

            borrowItemService.returnBook(10L, regularUser.getId());

            assertTrue(borrowItem.isReturned());
            verify(bookRepository).save(book);
            verify(borrowItemRepository).save(borrowItem);
        }

        @Test
        @DisplayName("Should throw AccessDeniedException if unauthorized user attempts return")
        void returnBookUnauthorizedUser() {
            User stranger = User.builder().id(999L).build();
            when(borrowItemRepository.findById(10L)).thenReturn(Optional.of(borrowItem));
            when(securityUtils.getCurrentUser()).thenReturn(stranger);
            when(securityUtils.hasRole("LIBRARIAN")).thenReturn(false);

            assertThrows(AccessDeniedException.class, () ->
                    borrowItemService.returnBook(10L, stranger.getId()));

            verify(bookRepository, never()).save(any());
            verify(borrowItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalStateException if book already returned")
        void returnBookAlreadyReturned() {
            borrowItem.markAsReturned();

            when(borrowItemRepository.findById(10L)).thenReturn(Optional.of(borrowItem));
            when(securityUtils.getCurrentUser()).thenReturn(regularUser);
            when(securityUtils.hasRole("LIBRARIAN")).thenReturn(true);

            assertThrows(IllegalStateException.class, () ->
                    borrowItemService.returnBook(10L, regularUser.getId()));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException if item not found")
        void returnBookNotFound() {
            when(borrowItemRepository.findById(10L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> borrowItemService.returnBook(10L, regularUser.getId()));
        }
    }
    @Nested
    @DisplayName("getByUserId")
    class GetByUserIdTests {

        @Test
        @DisplayName("should return paginated results for user")
        void getByUserId_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BorrowItem> itemPage = new PageImpl<>(List.of(borrowItem));
            BorrowItemResponseDto responseDto = new BorrowItemResponseDto(10L, 100L, "Test Book", 2L, "user@example.com",
                    LocalDate.now().minusDays(5), LocalDate.now().plusDays(9), null, false);

            when(userRepository.existsById(2L)).thenReturn(true);
            doNothing().when(securityUtils).checkAccessPermissionForUser(2L);
            when(borrowItemRepository.findByUserId(2L, pageable)).thenReturn(itemPage);
            when(borrowMapper.toItemDto(borrowItem)).thenReturn(responseDto);

            BorrowItemPageResponseDto result = borrowItemService.getByUserId(2L, pageable);

            assertEquals(1, result.items().size());
            assertEquals(1, result.totalPages());
            assertEquals(1, result.totalItems());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException if user does not exist")
        void getByUserId_userNotFound() {
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.existsById(2L)).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () -> borrowItemService.getByUserId(2L, pageable));
        }

        @Test
        @DisplayName("should throw AccessDeniedException if user not allowed")
        void getByUserId_accessDenied() {
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.existsById(2L)).thenReturn(true);
            doThrow(new AccessDeniedException("Not allowed")).when(securityUtils).checkAccessPermissionForUser(2L);

            assertThrows(AccessDeniedException.class, () -> borrowItemService.getByUserId(2L, pageable));
        }
    }

    @Nested
    @DisplayName("getByBookId")
    class GetByBookIdTests {

        @Test
        @DisplayName("should return paginated results for book")
        void getByBookId_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BorrowItem> itemPage = new PageImpl<>(List.of(borrowItem));
            BorrowItemResponseDto responseDto = new BorrowItemResponseDto(
                    10L, 100L, "Test Book", 2L, "user@example.com",
                    LocalDate.now().minusDays(5), LocalDate.now().plusDays(9), null, false
            );

            when(bookRepository.existsById(100L)).thenReturn(true);
            when(borrowItemRepository.findByBookId(100L, pageable)).thenReturn(itemPage);
            when(borrowMapper.toItemDto(borrowItem)).thenReturn(responseDto);

            BorrowItemPageResponseDto result = borrowItemService.getByBookId(100L, pageable);

            assertEquals(1, result.items().size());
            assertEquals(1, result.totalPages());
            assertEquals(1, result.totalItems());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException if book does not exist")
        void getByBookId_bookNotFound() {
            Pageable pageable = PageRequest.of(0, 10);
            when(bookRepository.existsById(999L)).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () -> borrowItemService.getByBookId(999L, pageable));
        }
    }

    @Nested
    @DisplayName("getOverdueItems")
    class GetOverdueItemsTests {

        @Test
        @DisplayName("should return paginated overdue borrow items")
        void getOverdueItems_success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BorrowItem> itemPage = new PageImpl<>(List.of(borrowItem));
            BorrowItemResponseDto responseDto = new BorrowItemResponseDto(
                    10L, 100L, "Test Book", 2L, "user@example.com",
                    LocalDate.now().minusDays(10), LocalDate.now().minusDays(1), null, false
            );

            when(borrowItemRepository.findOverdueItemsPageable(pageable)).thenReturn(itemPage);
            when(borrowMapper.toItemDto(borrowItem)).thenReturn(responseDto);

            BorrowItemPageResponseDto result = borrowItemService.getOverdueItems(pageable);

            assertEquals(1, result.items().size());
            assertEquals(1, result.totalPages());
            assertEquals(1, result.totalItems());
        }
    }

    @Nested
    @DisplayName("getActiveItemsByUser")
    class GetActiveItemsByUserTests {

        @Test
        @DisplayName("should return active borrow items for user")
        void getActiveItemsByUser_success() {
            List<BorrowItem> items = List.of(borrowItem);
            List<BorrowItemResponseDto> responseList = List.of(
                    new BorrowItemResponseDto(
                            10L, 100L, "Test Book", 2L, "user@example.com",
                            LocalDate.now().minusDays(5), LocalDate.now().plusDays(9), null, false)
            );

            when(userRepository.existsById(2L)).thenReturn(true);
            doNothing().when(securityUtils).checkAccessPermissionForUser(2L);
            when(borrowItemRepository.findByUserIdAndReturnedFalse(2L)).thenReturn(items);
            when(borrowMapper.toItemDtoList(items)).thenReturn(responseList);

            List<BorrowItemResponseDto> result = borrowItemService.getActiveItemsByUser(2L);

            assertEquals(1, result.size());
            assertEquals(10L, result.getFirst().id());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException if user not found")
        void getActiveItemsByUser_userNotFound() {
            when(userRepository.existsById(2L)).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () ->
                    borrowItemService.getActiveItemsByUser(2L));
        }

        @Test
        @DisplayName("should throw AccessDeniedException if user not authorized")
        void getActiveItemsByUser_accessDenied() {
            when(userRepository.existsById(2L)).thenReturn(true);
            doThrow(new AccessDeniedException("Not allowed")).when(securityUtils).checkAccessPermissionForUser(2L);

            assertThrows(AccessDeniedException.class, () ->
                    borrowItemService.getActiveItemsByUser(2L));
        }
    }

    @Nested
    @DisplayName("getBorrowItemsByDateRange")
    class GetByDateRangeTests {

        @Test
        @DisplayName("should return items when both dates are null")
        void dateRange_nullDates_returnsDefaults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BorrowItem> page = new PageImpl<>(List.of(borrowItem));
            BorrowItemResponseDto responseDto = new BorrowItemResponseDto(
                    10L, 100L, "Test Book", 2L, "user@example.com",
                    LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), null, false
            );

            when(borrowItemRepository.findByBorrowDateBetween(any(), any(), eq(pageable))).thenReturn(page);
            when(borrowMapper.toItemDto(borrowItem)).thenReturn(responseDto);

            BorrowItemPageResponseDto result = borrowItemService.getBorrowItemsByDateRange(null, null, pageable);

            assertEquals(1, result.items().size());
        }

        @Test
        @DisplayName("should throw exception if startDate is after endDate")
        void dateRange_invalidDates_throws() {
            Pageable pageable = PageRequest.of(0, 10);
            LocalDate start = LocalDate.of(2025, 5, 20);
            LocalDate end = LocalDate.of(2025, 5, 10);

            assertThrows(IllegalArgumentException.class, () ->
                    borrowItemService.getBorrowItemsByDateRange(start, end, pageable));
        }

        @Test
        @DisplayName("should return items for valid date range")
        void dateRange_validRange_success() {
            Pageable pageable = PageRequest.of(0, 10);
            LocalDate start = LocalDate.now().minusDays(15);
            LocalDate end = LocalDate.now();

            Page<BorrowItem> page = new PageImpl<>(List.of(borrowItem));
            BorrowItemResponseDto dto = new BorrowItemResponseDto(
                    10L, 100L, "Test Book", 2L, "user@example.com",
                    borrowItem.getBorrowDate(), borrowItem.getDueDate(), null, false
            );

            when(borrowItemRepository.findByBorrowDateBetween(start, end, pageable)).thenReturn(page);
            when(borrowMapper.toItemDto(borrowItem)).thenReturn(dto);

            BorrowItemPageResponseDto result = borrowItemService.getBorrowItemsByDateRange(start, end, pageable);

            assertEquals(1, result.items().size());
            assertEquals(10L, result.items().getFirst().id());
        }
    }

    @Nested
    @DisplayName("existsOverdueItemsByUserId")
    class ExistsOverdueItemsTests {

        @Test
        @DisplayName("should return true if there is at least one overdue item")
        void existsOverdueItems_true() {
            BorrowItem overdueItem = BorrowItem.builder()
                    .id(11L)
                    .user(regularUser)
                    .book(book)
                    .borrowDate(LocalDate.now().minusDays(10))
                    .dueDate(LocalDate.now().minusDays(1)) // overdue
                    .returned(false)
                    .build();

            when(userRepository.existsById(2L)).thenReturn(true);
            doNothing().when(securityUtils).checkAccessPermissionForUser(2L);
            when(borrowItemRepository.findByUserIdAndReturnedFalse(2L))
                    .thenReturn(List.of(overdueItem));

            boolean result = borrowItemService.existsOverdueItemsByUserId(2L);

            assertTrue(result);
        }

        @Test
        @DisplayName("should return false if no overdue items exist")
        void existsOverdueItems_false() {
            BorrowItem notOverdueItem = BorrowItem.builder()
                    .id(12L)
                    .user(regularUser)
                    .book(book)
                    .borrowDate(LocalDate.now().minusDays(5))
                    .dueDate(LocalDate.now().plusDays(2)) // not overdue
                    .returned(false)
                    .build();

            when(userRepository.existsById(2L)).thenReturn(true);
            doNothing().when(securityUtils).checkAccessPermissionForUser(2L);
            when(borrowItemRepository.findByUserIdAndReturnedFalse(2L))
                    .thenReturn(List.of(notOverdueItem));

            boolean result = borrowItemService.existsOverdueItemsByUserId(2L);

            assertFalse(result);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException if user not found")
        void existsOverdueItems_userNotFound() {
            when(userRepository.existsById(2L)).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () ->
                    borrowItemService.existsOverdueItemsByUserId(2L));
        }

        @Test
        @DisplayName("should throw AccessDeniedException if user unauthorized")
        void existsOverdueItems_accessDenied() {
            when(userRepository.existsById(2L)).thenReturn(true);
            doThrow(new AccessDeniedException("denied")).when(securityUtils).checkAccessPermissionForUser(2L);

            assertThrows(AccessDeniedException.class, () ->
                    borrowItemService.existsOverdueItemsByUserId(2L));
        }
    }
    @Nested
    @DisplayName("countActiveItemsByUser")
    class CountActiveItemsTests {

        @Test
        @DisplayName("should return count of active items for user")
        void countActiveItems_success() {
            when(userRepository.existsById(2L)).thenReturn(true);
            doNothing().when(securityUtils).checkAccessPermissionForUser(2L);
            when(borrowItemRepository.countActiveByUserId(2L)).thenReturn(3);

            int result = borrowItemService.countActiveItemsByUser(2L);

            assertEquals(3, result);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException if user not found")
        void countActiveItems_userNotFound() {
            when(userRepository.existsById(2L)).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () ->
                    borrowItemService.countActiveItemsByUser(2L));
        }

        @Test
        @DisplayName("should throw AccessDeniedException if user unauthorized")
        void countActiveItems_accessDenied() {
            when(userRepository.existsById(2L)).thenReturn(true);
            doThrow(new AccessDeniedException("forbidden")).when(securityUtils).checkAccessPermissionForUser(2L);

            assertThrows(AccessDeniedException.class, () ->
                    borrowItemService.countActiveItemsByUser(2L));
        }
    }
}