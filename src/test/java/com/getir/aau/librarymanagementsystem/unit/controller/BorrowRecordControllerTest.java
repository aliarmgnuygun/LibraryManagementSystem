package com.getir.aau.librarymanagementsystem.unit.controller;

import com.getir.aau.librarymanagementsystem.controller.BorrowRecordController;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.BorrowItemRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.request.BorrowRecordRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowRecordResponseDto;
import com.getir.aau.librarymanagementsystem.service.BorrowRecordService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BorrowRecordController Unit Tests")
public class BorrowRecordControllerTest {

    @Mock
    private BorrowRecordService borrowRecordService;

    @InjectMocks
    private BorrowRecordController borrowRecordController;

    private BorrowRecordRequestDto requestDto;
    private BorrowRecordResponseDto responseDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        List<BorrowItemRequestDto> itemRequestList = List.of(new BorrowItemRequestDto(1L), new BorrowItemRequestDto(2L));
        requestDto = new BorrowRecordRequestDto(1L, itemRequestList);

        List<BorrowItemResponseDto> itemResponseList = List.of();
        LocalDate borrowDate = LocalDate.now();
        responseDto = new BorrowRecordResponseDto(1L, 1L, "John Doe", borrowDate, borrowDate.plusDays(14), itemResponseList);

        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("POST /api/borrow-records")
    class BorrowBooksTests {

        @Test
        @DisplayName("should return 201 CREATED when books are borrowed successfully")
        void shouldReturnCreatedStatusWhenBooksAreBorrowedSuccessfully() {
            when(borrowRecordService.borrowBooks(any(BorrowRecordRequestDto.class))).thenReturn(responseDto);

            ResponseEntity<BorrowRecordResponseDto> response = borrowRecordController.borrowBooks(requestDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(1L);
            verify(borrowRecordService).borrowBooks(requestDto);
        }

        @Test
        @DisplayName("should throw exception when input is invalid")
        void shouldThrowExceptionWhenInputIsInvalid() {
            when(borrowRecordService.borrowBooks(any(BorrowRecordRequestDto.class)))
                    .thenThrow(new IllegalArgumentException("Invalid borrow record data"));

            assertThrows(IllegalArgumentException.class, () -> borrowRecordController.borrowBooks(requestDto));
            verify(borrowRecordService).borrowBooks(requestDto);
        }

        @Test
        @DisplayName("should throw exception when user is not eligible to borrow")
        void shouldThrowExceptionWhenUserIsNotEligibleToBorrow() {
            when(borrowRecordService.borrowBooks(any(BorrowRecordRequestDto.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, ("User has reached maximum allowed borrowing limit")));

            assertThrows(ResponseStatusException.class, () -> borrowRecordController.borrowBooks(requestDto));
            verify(borrowRecordService).borrowBooks(requestDto);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-records/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("should return 200 OK when borrow record is found")
        void shouldReturnOkStatusWhenBorrowRecordIsFound() {
            when(borrowRecordService.getById(anyLong())).thenReturn(responseDto);

            ResponseEntity<BorrowRecordResponseDto> response = borrowRecordController.getById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(1L);
            verify(borrowRecordService).getById(1L);
        }

        @Test
        @DisplayName("should throw exception when borrow record does not exist")
        void shouldThrowExceptionWhenBorrowRecordDoesNotExist() {
            when(borrowRecordService.getById(999L))
                    .thenThrow(new ResourceNotFoundException("BorrowRecord", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> borrowRecordController.getById(999L));
            verify(borrowRecordService).getById(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-records")
    class GetAllTests {

        @Test
        @DisplayName("should return 200 OK with page of borrow records")
        void shouldReturnOkStatusWithPageOfBorrowRecords() {
            Page<BorrowRecordResponseDto> page = new PageImpl<>(List.of(responseDto), pageable, 1);
            when(borrowRecordService.getAll(any(Pageable.class))).thenReturn(page);

            ResponseEntity<Page<BorrowRecordResponseDto>> response = borrowRecordController.getAll(pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().getFirst().id()).isEqualTo(1L);
            verify(borrowRecordService).getAll(pageable);
        }

        @Test
        @DisplayName("should return 200 OK with empty page when no borrow records exist")
        void shouldReturnOkStatusWithEmptyPageWhenNoBorrowRecordsExist() {
            Page<BorrowRecordResponseDto> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(borrowRecordService.getAll(any(Pageable.class))).thenReturn(emptyPage);

            ResponseEntity<Page<BorrowRecordResponseDto>> response = borrowRecordController.getAll(pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isEmpty();
            verify(borrowRecordService).getAll(pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-records/user/{userId}")
    class GetByUserTests {

        @Test
        @DisplayName("should return 200 OK with page of user's borrow records")
        void shouldReturnOkStatusWithPageOfUserBorrowRecords() {
            Page<BorrowRecordResponseDto> page = new PageImpl<>(List.of(responseDto), pageable, 1);
            when(borrowRecordService.getByUser(anyLong(), any(Pageable.class))).thenReturn(page);

            ResponseEntity<Page<BorrowRecordResponseDto>> response = borrowRecordController.getByUser(1L, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            verify(borrowRecordService).getByUser(1L, pageable);
        }

        @Test
        @DisplayName("should throw exception when user does not exist")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            when(borrowRecordService.getByUser(eq(999L), any(Pageable.class)))
                    .thenThrow(new ResourceNotFoundException("User", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> borrowRecordController.getByUser(999L, pageable));
            verify(borrowRecordService).getByUser(999L, pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-records/user/{userId}/active")
    class GetActiveByUserTests {

        @Test
        @DisplayName("should return 200 OK with list of user's active borrow records")
        void shouldReturnOkStatusWithListOfUserActiveBorrowRecords() {
            List<BorrowRecordResponseDto> records = List.of(responseDto);
            when(borrowRecordService.getActiveRecordsByUser(anyLong())).thenReturn(records);

            ResponseEntity<List<BorrowRecordResponseDto>> response = borrowRecordController.getActiveByUser(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            verify(borrowRecordService).getActiveRecordsByUser(1L);
        }

        @Test
        @DisplayName("should return 200 OK with empty list when user has no active borrow records")
        void shouldReturnOkStatusWithEmptyListWhenUserHasNoActiveBorrowRecords() {
            when(borrowRecordService.getActiveRecordsByUser(anyLong())).thenReturn(Collections.emptyList());

            ResponseEntity<List<BorrowRecordResponseDto>> response = borrowRecordController.getActiveByUser(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
            verify(borrowRecordService).getActiveRecordsByUser(1L);
        }

        @Test
        @DisplayName("should throw exception when user does not exist")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            when(borrowRecordService.getActiveRecordsByUser(999L))
                    .thenThrow(new ResourceNotFoundException("User", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> borrowRecordController.getActiveByUser(999L));
            verify(borrowRecordService).getActiveRecordsByUser(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-records/filter")
    class FilterTests {

        @Test
        @DisplayName("should return 200 OK when filtering by email")
        void shouldReturnOkStatusWhenFilteringByEmail() {
            Page<BorrowRecordResponseDto> page = new PageImpl<>(List.of(responseDto), pageable, 1);
            when(borrowRecordService.filter(eq("user@example.com"), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            ResponseEntity<Page<BorrowRecordResponseDto>> response =
                    borrowRecordController.filter("user@example.com", null, null, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            verify(borrowRecordService).filter("user@example.com", null, null, pageable);
        }

        @Test
        @DisplayName("should return 200 OK when filtering by date range")
        void shouldReturnOkStatusWhenFilteringByDateRange() {
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            Page<BorrowRecordResponseDto> page = new PageImpl<>(List.of(responseDto), pageable, 1);
            when(borrowRecordService.filter(isNull(), eq(startDate), eq(endDate), any(Pageable.class)))
                    .thenReturn(page);

            ResponseEntity<Page<BorrowRecordResponseDto>> response =
                    borrowRecordController.filter(null, startDate, endDate, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).hasSize(1);
            verify(borrowRecordService).filter(null, startDate, endDate, pageable);
        }

        @Test
        @DisplayName("should return 200 OK with empty page when no records match filter criteria")
        void shouldReturnOkStatusWithEmptyPageWhenNoRecordsMatchFilterCriteria() {
            Page<BorrowRecordResponseDto> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(borrowRecordService.filter(anyString(), any(), any(), any(Pageable.class))).thenReturn(emptyPage);

            ResponseEntity<Page<BorrowRecordResponseDto>> response =
                    borrowRecordController.filter("nonexistent@example.com", null, null, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isEmpty();
            verify(borrowRecordService).filter("nonexistent@example.com", null, null, pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-records/check-eligibility/{userId}")
    class CheckEligibilityTests {

        @Test
        @DisplayName("should return 200 OK when user is eligible to borrow")
        void shouldReturnOkStatusWhenUserIsEligibleToBorrow() {
            doNothing().when(borrowRecordService).checkBorrowEligibility(anyLong());

            ResponseEntity<Void> response = borrowRecordController.checkEligibility(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(borrowRecordService).checkBorrowEligibility(1L);
        }

        @Test
        @DisplayName("should throw exception when user is not eligible to borrow")
        void shouldThrowExceptionWhenUserIsNotEligibleToBorrow() {
            doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "User has reached maximum allowed borrowing limit"))
                    .when(borrowRecordService).checkBorrowEligibility(anyLong());

            assertThrows(ResponseStatusException.class, () -> borrowRecordController.checkEligibility(1L));
            verify(borrowRecordService).checkBorrowEligibility(1L);
        }

        @Test
        @DisplayName("should throw exception when user does not exist")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            doThrow(new ResourceNotFoundException("User", "id", 999L))
                    .when(borrowRecordService).checkBorrowEligibility(999L);

            assertThrows(ResourceNotFoundException.class, () -> borrowRecordController.checkEligibility(999L));
            verify(borrowRecordService).checkBorrowEligibility(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-records/book-availability/{bookId}")
    class BookAvailabilityTests {

        @Test
        @DisplayName("should return 200 OK when book is available for borrowing")
        void shouldReturnOkStatusWhenBookIsAvailable() {
            when(borrowRecordService.isBookAvailableForBorrowing(anyLong())).thenReturn(true);

            ResponseEntity<Void> response = borrowRecordController.isBookAvailable(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(borrowRecordService).isBookAvailableForBorrowing(1L);
        }

        @Test
        @DisplayName("should return 409 CONFLICT when book is not available for borrowing")
        void shouldReturnConflictStatusWhenBookIsNotAvailable() {
            when(borrowRecordService.isBookAvailableForBorrowing(anyLong())).thenReturn(false);

            ResponseEntity<Void> response = borrowRecordController.isBookAvailable(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            verify(borrowRecordService).isBookAvailableForBorrowing(1L);
        }
    }
}