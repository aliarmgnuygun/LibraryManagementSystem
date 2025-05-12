package com.getir.aau.librarymanagementsystem.unit.service;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.BorrowItemRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.request.BorrowRecordRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowRecordResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.*;
import com.getir.aau.librarymanagementsystem.model.mapper.BorrowMapper;
import com.getir.aau.librarymanagementsystem.repository.*;
import com.getir.aau.librarymanagementsystem.security.SecurityUtils;
import com.getir.aau.librarymanagementsystem.service.impl.BorrowRecordServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowRecordServiceImplTest {

    @Mock BorrowRecordRepository borrowRecordRepository;
    @Mock BorrowItemRepository borrowItemRepository;
    @Mock UserRepository userRepository;
    @Mock BookRepository bookRepository;
    @Mock BorrowMapper borrowMapper;
    @Mock SecurityUtils securityUtils;
    @InjectMocks BorrowRecordServiceImpl borrowRecordService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .role(new Role(1L, ERole.ROLE_USER, "ROLE_USER"))
                .email("user@example.com")
                .build();

        book = Book.builder()
                .id(1L)
                .available(true)
                .title("Test Book")
                .numberOfCopies(1)
                .build();
    }

    @Nested
    class BorrowBooksTests {
        @Test
        @DisplayName("Should return true when book is available")
        void shouldBorrowBooksSuccessfully() {
            BorrowItemRequestDto itemDto = new BorrowItemRequestDto(book.getId());
            BorrowRecordRequestDto recordRequestDto = new BorrowRecordRequestDto(user.getId(), List.of(itemDto));
            BorrowRecordResponseDto expectedResponse = mock(BorrowRecordResponseDto.class);

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
            when(borrowMapper.toRecordDto(any())).thenReturn(expectedResponse);

            BorrowRecordResponseDto response = borrowRecordService.borrowBooks(recordRequestDto);

            assertNotNull(response);
            verify(userRepository).findById(user.getId());
            verify(bookRepository).findById(book.getId());
            verify(bookRepository).saveAll(anyList());
            verify(borrowRecordRepository).save(any());
            verify(borrowItemRepository).saveAll(anyList());

            assertFalse(book.isAvailable());
        }

        @Test
        @DisplayName("Should throw when user is not found")
        void shouldThrowWhenUserNotFound() {
            BorrowItemRequestDto itemDto = new BorrowItemRequestDto(book.getId());
            BorrowRecordRequestDto recordRequestDto = new BorrowRecordRequestDto(user.getId(), List.of(itemDto));

            when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> borrowRecordService.borrowBooks(recordRequestDto));

            verify(userRepository).findById(user.getId());
        }

        @Test
        @DisplayName("Should throw when book is not found")
        void shouldThrowWhenBookNotFound() {
            BorrowItemRequestDto itemDto = new BorrowItemRequestDto(book.getId());
            BorrowRecordRequestDto recordRequestDto = new BorrowRecordRequestDto(user.getId(), List.of(itemDto));

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> borrowRecordService.borrowBooks(recordRequestDto));

            verify(userRepository).findById(user.getId());
            verify(bookRepository).findById(book.getId());
        }

        @Test
        @DisplayName("Should throw when book is not available")
        void shouldThrowWhenBookNotAvailable() {
            book = Book.builder()
                    .id(1L)
                    .title("Unavailable Book")
                    .numberOfCopies(0)
                    .available(false)
                    .build();
            BorrowItemRequestDto itemDto = new BorrowItemRequestDto(book.getId());
            BorrowRecordRequestDto recordRequestDto = new BorrowRecordRequestDto(user.getId(), List.of(itemDto));

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

            assertThrows(ResourceAlreadyExistsException.class,
                    () -> borrowRecordService.borrowBooks(recordRequestDto));

            verify(userRepository).findById(user.getId());
            verify(bookRepository).findById(book.getId());
        }
    }

    @Nested
    class EligibilityTests {

        @Test
        @DisplayName("Should throw when user exceeds borrowing limit")
        void shouldThrowWhenUserExceedsBookLimit() {
            List<BorrowItem> borrowedItems = List.of(
                    new BorrowItem(), new BorrowItem(),
                    new BorrowItem(), new BorrowItem(),
                    new BorrowItem()
            );

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(borrowItemRepository.findByUserIdAndReturnedFalse(user.getId()))
                    .thenReturn(borrowedItems);

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> borrowRecordService.checkBorrowEligibility(user.getId())
            );

            assertEquals("You have reached the maximum borrowing limit.", exception.getMessage());

            verify(userRepository).findById(user.getId());
            verify(borrowItemRepository).findByUserIdAndReturnedFalse(user.getId());
        }

        @Test
        @DisplayName("Should throw when user has overdue items")
        void shouldThrowWhenUserHasOverdueItems() {
            BorrowItem overdueItem = BorrowItem.builder()
                    .dueDate(LocalDate.now().minusDays(1))
                    .build();

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(borrowItemRepository.findByUserIdAndReturnedFalse(user.getId()))
                    .thenReturn(List.of(overdueItem));

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> borrowRecordService.checkBorrowEligibility(user.getId())
            );

            assertEquals("You cannot borrow books until overdue items are returned.", exception.getMessage());

            verify(userRepository).findById(user.getId());
            verify(borrowItemRepository).findByUserIdAndReturnedFalse(user.getId());
        }

        @Test
        @DisplayName("Should not throw when user is eligible to borrow books")
        void shouldAllowBorrowingWhenEligible() {
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(borrowItemRepository.findByUserIdAndReturnedFalse(user.getId()))
                    .thenReturn(List.of());

            assertDoesNotThrow(() -> borrowRecordService.checkBorrowEligibility(user.getId()));

            verify(userRepository).findById(user.getId());
            verify(borrowItemRepository).findByUserIdAndReturnedFalse(user.getId());
        }
    }

    @Nested
    class AuthorizationTests {
        @Test
        @DisplayName("Should throw if user is not authorized to access record")
        void shouldThrowIfUnauthorizedToAccessRecord() {
            BorrowRecord record = BorrowRecord.builder()
                    .id(1L)
                    .user(User.builder().id(2L).build())
                    .build();

            when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(securityUtils.isLibrarian()).thenReturn(false);
            when(securityUtils.getCurrentUserEmail()).thenReturn("user@example.com");
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

            assertThrows(AccessDeniedException.class,
                    () -> borrowRecordService.getById(1L));

            verify(borrowRecordRepository).findById(1L);
            verify(securityUtils).isLibrarian();
            verify(securityUtils).getCurrentUserEmail();
            verify(userRepository).findByEmail("user@example.com");
        }

        @Test
        @DisplayName("Should allow librarian to access any record")
        void shouldAllowLibrarianToAccessAnyRecord() {
            BorrowRecord record = BorrowRecord.builder()
                    .id(1L)
                    .user(User.builder().id(2L).build())
                    .build();
            BorrowRecordResponseDto expectedDto = mock(BorrowRecordResponseDto.class);

            when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(securityUtils.isLibrarian()).thenReturn(true);
            when(borrowMapper.toRecordDto(record)).thenReturn(expectedDto);

            BorrowRecordResponseDto result = borrowRecordService.getById(1L);

            assertNotNull(result);
            verify(borrowRecordRepository).findById(1L);
            verify(securityUtils).isLibrarian();
            verify(borrowMapper).toRecordDto(record);
        }
    }
}