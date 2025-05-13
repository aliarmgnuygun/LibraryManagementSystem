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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    class CheckUserEligibilityTests {
        @Test
        @DisplayName("Should throw when user is not a regular user")
        void shouldThrowWhenUserIsNotRegularUser() {
            User librarian = User.builder()
                    .id(2L)
                    .role(new Role(2L, ERole.ROLE_LIBRARIAN, "ROLE_LIBRARIAN"))
                    .build();

            when(userRepository.findById(librarian.getId())).thenReturn(Optional.of(librarian));

            assertThrows(AccessDeniedException.class,
                    () -> borrowRecordService.borrowBooks(new BorrowRecordRequestDto(librarian.getId(), List.of())));

            verify(userRepository).findById(librarian.getId());
        }

        @Test
        @DisplayName("Should throw when role is not found")
        void shouldThrowWhenRoleIsNotFound() {
            User userWithoutRole = User.builder()
                    .id(3L)
                    .build();

            when(userRepository.findById(userWithoutRole.getId())).thenReturn(Optional.of(userWithoutRole));

            assertThrows(NullPointerException.class,
                    () -> borrowRecordService.borrowBooks(new BorrowRecordRequestDto(userWithoutRole.getId(), List.of())));

            verify(userRepository).findById(userWithoutRole.getId());
        }

        @Test
        @DisplayName("Should not throw when user has some books but under limit")
        void shouldNotThrowWhenUserHasSomeBooksButUnderLimit() {
            List<BorrowItem> twoActiveItems = List.of(
                    BorrowItem.builder().dueDate(LocalDate.now().plusDays(5)).build(),
                    BorrowItem.builder().dueDate(LocalDate.now().plusDays(3)).build()
            );

            BorrowItemRequestDto itemDto = new BorrowItemRequestDto(book.getId());
            BorrowRecordRequestDto recordRequestDto = new BorrowRecordRequestDto(user.getId(), List.of(itemDto));

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(borrowItemRepository.findByUserIdAndReturnedFalse(user.getId())).thenReturn(twoActiveItems);
            when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
            when(borrowMapper.toRecordDto(any())).thenReturn(mock(BorrowRecordResponseDto.class));

            assertDoesNotThrow(() -> borrowRecordService.borrowBooks(recordRequestDto));

            verify(userRepository).findById(user.getId());
            verify(borrowItemRepository).findByUserIdAndReturnedFalse(user.getId());
        }

        @Test
        @DisplayName("Should throw when user has exactly maximum number of books")
        void shouldThrowWhenUserHasMaximumBooks() {
            List<BorrowItem> fiveActiveItems = List.of(
                    mock(BorrowItem.class), mock(BorrowItem.class),
                    mock(BorrowItem.class), mock(BorrowItem.class),
                    mock(BorrowItem.class)
            );

            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
            when(borrowItemRepository.findByUserIdAndReturnedFalse(user.getId())).thenReturn(fiveActiveItems);

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> borrowRecordService.checkBorrowEligibility(user.getId())
            );

            assertEquals("You have reached the maximum borrowing limit.", exception.getMessage());
        }
    }

        @Nested
    class GetActiveRecordsByUserTests {
        @Test
        @DisplayName("Should return active records for user")
        void shouldReturnActiveRecordsForUser() {
            BorrowItem activeItem = BorrowItem.builder()
                    .returned(false)
                    .build();

            BorrowItem returnedItem = BorrowItem.builder()
                    .returned(true)
                    .build();

            BorrowRecord activeRecord = BorrowRecord.builder()
                    .id(1L)
                    .user(user)
                    .items(List.of(activeItem, returnedItem))
                    .build();

            List<BorrowRecord> records = List.of(activeRecord);

            @SuppressWarnings("unchecked")
            Page<BorrowRecord> mockPage = (Page<BorrowRecord>) mock(Page.class);
            when(mockPage.getContent()).thenReturn(records);

            when(securityUtils.getCurrentUser()).thenReturn(user);
            when(securityUtils.hasRole("LIBRARIAN")).thenReturn(false);
            when(userRepository.existsById(user.getId())).thenReturn(true);
            when(borrowRecordRepository.findByUserId(eq(user.getId()), any(Pageable.class))).thenReturn(mockPage);
            when(borrowMapper.toRecordDtoList(anyList())).thenReturn(List.of(mock(BorrowRecordResponseDto.class)));

            List<BorrowRecordResponseDto> result = borrowRecordService.getActiveRecordsByUser(user.getId());

            assertNotNull(result);
            assertFalse(result.isEmpty());
            verify(borrowMapper).toRecordDtoList(anyList());
        }

        @Test
        @DisplayName("Should throw when unauthorized user tries to access another user's records")
        void shouldThrowWhenUnauthorizedAccess() {
            User otherUser = User.builder().id(2L).build();

            when(securityUtils.getCurrentUser()).thenReturn(user);
            when(securityUtils.hasRole("LIBRARIAN")).thenReturn(false);

            assertThrows(AccessDeniedException.class,
                    () -> borrowRecordService.getActiveRecordsByUser(otherUser.getId()));

            verify(securityUtils).getCurrentUser();
            verify(securityUtils).hasRole("LIBRARIAN");
        }

        @Test
        @DisplayName("Should throw when user does not exist")
        void shouldThrowWhenUserDoesNotExist() {
            Long nonExistentUserId = 999L;

            when(securityUtils.getCurrentUser()).thenReturn(user);
            when(securityUtils.hasRole("LIBRARIAN")).thenReturn(true);
            when(userRepository.existsById(nonExistentUserId)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> borrowRecordService.getActiveRecordsByUser(nonExistentUserId));

            verify(userRepository).existsById(nonExistentUserId);
        }

        @Test
        @DisplayName("Should return empty list when user has no active records")
        void shouldReturnEmptyListWhenNoActiveRecords() {
            BorrowItem returnedItem = BorrowItem.builder()
                    .returned(true)
                    .build();

            BorrowRecord inactiveRecord = BorrowRecord.builder()
                    .id(1L)
                    .user(user)
                    .items(List.of(returnedItem))
                    .build();

            List<BorrowRecord> records = List.of(inactiveRecord);

            @SuppressWarnings("unchecked")
            Page<BorrowRecord> mockPage = (Page<BorrowRecord>) mock(Page.class);
            when(mockPage.getContent()).thenReturn(records);

            when(securityUtils.getCurrentUser()).thenReturn(user);
            when(securityUtils.hasRole("LIBRARIAN")).thenReturn(false);
            when(userRepository.existsById(user.getId())).thenReturn(true);
            when(borrowRecordRepository.findByUserId(eq(user.getId()), any(Pageable.class))).thenReturn(mockPage);
            when(borrowMapper.toRecordDtoList(eq(List.of()))).thenReturn(List.of());

            List<BorrowRecordResponseDto> result = borrowRecordService.getActiveRecordsByUser(user.getId());

            assertTrue(result.isEmpty());
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

            User currentUser = User.builder().id(1L).email("user@example.com").build();

            when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(securityUtils.hasRole("LIBRARIAN")).thenReturn(false);
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);

            assertThrows(AccessDeniedException.class,
                    () -> borrowRecordService.getById(1L));

            verify(borrowRecordRepository).findById(1L);
            verify(securityUtils).hasRole("LIBRARIAN");
            verify(securityUtils).getCurrentUser();
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
            when(securityUtils.hasRole("LIBRARIAN")).thenReturn(true);
            when(borrowMapper.toRecordDto(record)).thenReturn(expectedDto);

            BorrowRecordResponseDto result = borrowRecordService.getById(1L);

            assertNotNull(result);
            verify(borrowRecordRepository).findById(1L);
            verify(securityUtils).hasRole("LIBRARIAN");
            verify(borrowMapper).toRecordDto(record);
        }
    }
}