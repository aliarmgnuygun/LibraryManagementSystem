package com.getir.aau.librarymanagementsystem.unit.controller;

import com.getir.aau.librarymanagementsystem.controller.BorrowItemController;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemResponseDto;
import com.getir.aau.librarymanagementsystem.service.BorrowItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BorrowItemController Unit Tests")
public class BorrowItemControllerTest {

    @Mock
    private BorrowItemService borrowItemService;

    @InjectMocks
    private BorrowItemController borrowItemController;

    private Pageable pageable;
    private BorrowItemPageResponseDto pageResponseDto;
    private List<BorrowItemResponseDto> itemResponseList;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        BorrowItemResponseDto itemResponseDto = new BorrowItemResponseDto(
                1L, 1L, "Book Name", 1L, "user@example.com", LocalDate.now(),
                LocalDate.now().plusDays(14), null, false);

        itemResponseList = new ArrayList<>();
        itemResponseList.add(itemResponseDto);

        pageResponseDto = new BorrowItemPageResponseDto(itemResponseList, 1, 10);
    }

    @Nested
    @DisplayName("PUT /api/borrow-items/{itemId}/return")
    class ReturnBookTests {

        @Test
        @DisplayName("should return 204 No Content when book is returned successfully")
        void shouldReturn204NoContentWhenBookIsReturnedSuccessfully() {
            doNothing().when(borrowItemService).returnBook(anyLong(), anyLong());

            ResponseEntity<Void> response = borrowItemController.returnBook(1L, 1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(borrowItemService).returnBook(1L, 1L);
        }

        @Test
        @DisplayName("should throw exception when book is already returned")
        void shouldThrowExceptionWhenBookIsAlreadyReturned() {
            doThrow(new IllegalStateException("The book is already returned")).when(borrowItemService).returnBook(anyLong(), anyLong());

            assertThrows(IllegalStateException.class, () -> borrowItemController.returnBook(1L, 1L));
            verify(borrowItemService).returnBook(1L, 1L);
        }

        @Test
        @DisplayName("should throw exception when item ID is invalid")
        void shouldThrowExceptionWhenBorrowItemNotFound() {
            doThrow(new ResourceNotFoundException("BorrowItem", "id", 999L)).when(borrowItemService).returnBook(eq(999L), anyLong());

            assertThrows(ResourceNotFoundException.class, () -> borrowItemController.returnBook(999L, 1L));
            verify(borrowItemService).returnBook(999L, 1L);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-items/user/{userId}")
    class GetByUserIdTests {

        @Test
        @DisplayName("should return 200 OK with user's borrow items")
        void shouldReturn200OkWithUserBorrowItems() {
            when(borrowItemService.getByUserId(anyLong(), any(Pageable.class))).thenReturn(pageResponseDto);

            ResponseEntity<BorrowItemPageResponseDto> response = borrowItemController.getByUserId(1L, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().items()).hasSize(1);
            verify(borrowItemService).getByUserId(1L, pageable);
        }

        @Test
        @DisplayName("should return 200 OK with empty list when no borrow items found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(borrowItemService.getByUserId(eq(999L), any(Pageable.class)))
                    .thenThrow(new ResourceNotFoundException("User", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> borrowItemController.getByUserId(999L, pageable));
            verify(borrowItemService).getByUserId(999L, pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-items/book/{bookId}")
    class GetByBookIdTests {

        @Test
        @DisplayName("should return 200 OK with book borrow items")
        void shouldReturn200OkWithBookBorrowItems() {
            when(borrowItemService.getByBookId(anyLong(), any(Pageable.class))).thenReturn(pageResponseDto);

            ResponseEntity<BorrowItemPageResponseDto> response = borrowItemController.getByBookId(1L, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().items()).hasSize(1);
            verify(borrowItemService).getByBookId(1L, pageable);
        }

        @Test
        @DisplayName("should return 200 OK with empty list when no borrow items found")
        void shouldThrowExceptionWhenBookNotFound() {
            when(borrowItemService.getByBookId(eq(999L), any(Pageable.class)))
                    .thenThrow(new ResourceNotFoundException("Book", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> borrowItemController.getByBookId(999L, pageable));
            verify(borrowItemService).getByBookId(999L, pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-items/overdue")
    class GetOverdueItemsTests {

        @Test
        @DisplayName("should return 200 OK with overdue items")
        void shouldReturn200OkWithOverdueItems() {
            when(borrowItemService.getOverdueItems(any(Pageable.class))).thenReturn(pageResponseDto);

            ResponseEntity<BorrowItemPageResponseDto> response = borrowItemController.getOverdueItems(pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().items()).hasSize(1);
            verify(borrowItemService).getOverdueItems(pageable);
        }

        @Test
        @DisplayName("should return 200 OK with empty list when no overdue items found")
        void shouldReturn200OkWithEmptyListWhenNoOverdueItemsFound() {
            BorrowItemPageResponseDto emptyPage = new BorrowItemPageResponseDto(Collections.emptyList(), 0, 0);
            when(borrowItemService.getOverdueItems(any(Pageable.class))).thenReturn(emptyPage);

            ResponseEntity<BorrowItemPageResponseDto> response = borrowItemController.getOverdueItems(pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().items()).isEmpty();
            verify(borrowItemService).getOverdueItems(pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-items/date-range")
    class GetByDateRangeTests {

        @Test
        @DisplayName("should return 200 OK with items in date range")
        void shouldReturn200OkWithDateRangeItems() {
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            when(borrowItemService.getBorrowItemsByDateRange(any(), any(), any(Pageable.class))).thenReturn(pageResponseDto);

            ResponseEntity<BorrowItemPageResponseDto> response = borrowItemController.getByDateRange(startDate, endDate, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().items()).hasSize(1);
            verify(borrowItemService).getBorrowItemsByDateRange(startDate, endDate, pageable);
        }

        @Test
        @DisplayName("should return empty list when no items found in date range")
        void shouldThrowExceptionWhenInvalidDateRange() {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().minusDays(30);

            when(borrowItemService.getBorrowItemsByDateRange(startDate, endDate, pageable))
                    .thenThrow(new IllegalArgumentException("Start date must be before end date"));

            assertThrows(IllegalArgumentException.class,
                    () -> borrowItemController.getByDateRange(startDate, endDate, pageable));
            verify(borrowItemService).getBorrowItemsByDateRange(startDate, endDate, pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-items/user/{userId}/active")
    class GetActiveItemsByUserTests {

        @Test
        @DisplayName("should return active items for user")
        void shouldReturnActiveItemsForUser() {
            when(borrowItemService.getActiveItemsByUser(anyLong())).thenReturn(itemResponseList);

            List<BorrowItemResponseDto> response = borrowItemController.getActiveItemsByUser(1L);

            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
            verify(borrowItemService).getActiveItemsByUser(1L);
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(borrowItemService.getActiveItemsByUser(999L))
                    .thenThrow(new ResourceNotFoundException("User", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> borrowItemController.getActiveItemsByUser(999L));
            verify(borrowItemService).getActiveItemsByUser(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-items/user/{userId}/exist-overdue")
    class ExistsOverdueItemsByUserIdTests {

        @Test
        @DisplayName("should return true when user has overdue items")
        void shouldReturnTrueWhenUserHasOverdueItems() {
            when(borrowItemService.existsOverdueItemsByUserId(anyLong())).thenReturn(true);

            ResponseEntity<Boolean> response = borrowItemController.existsOverdueItemsByUserId(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isTrue();
            verify(borrowItemService).existsOverdueItemsByUserId(1L);
        }

        @Test
        @DisplayName("should return false when user has no overdue items")
        void shouldReturnFalseWhenUserHasNoOverdueItems() {
            when(borrowItemService.existsOverdueItemsByUserId(anyLong())).thenReturn(false);

            ResponseEntity<Boolean> response = borrowItemController.existsOverdueItemsByUserId(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isFalse();
            verify(borrowItemService).existsOverdueItemsByUserId(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/borrow-items/user/{userId}/count-active")
    class CountActiveItemsByUserTests {

        @Test
        @DisplayName("should return active items count for user")
        void shouldReturnActiveItemsCountForUser() {
            when(borrowItemService.countActiveItemsByUser(anyLong())).thenReturn(3);

            ResponseEntity<Integer> response = borrowItemController.countActiveItemsByUser(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(3);
            verify(borrowItemService).countActiveItemsByUser(1L);
        }

        @Test
        @DisplayName("should return zero when user has no active items")
        void shouldReturnZeroWhenUserHasNoActiveItems() {
            when(borrowItemService.countActiveItemsByUser(anyLong())).thenReturn(0);

            ResponseEntity<Integer> response = borrowItemController.countActiveItemsByUser(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(0);
            verify(borrowItemService).countActiveItemsByUser(1L);
        }
    }
}