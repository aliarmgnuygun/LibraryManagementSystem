package com.getir.aau.librarymanagementsystem.controller;

import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemResponseDto;
import com.getir.aau.librarymanagementsystem.service.BorrowItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/borrow-items")
@RequiredArgsConstructor
@Tag(name = "Borrow Items", description = "API endpoints for managing book borrowing")
public class BorrowItemController {

    private final BorrowItemService borrowItemService;

    @Operation(summary = "Return a borrowed book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Book returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid item ID or book already returned"),
            @ApiResponse(responseCode = "404", description = "Borrow item not found")
    })
    @PutMapping("/{itemId}/return")
    public ResponseEntity<Void> returnBook(@PathVariable Long itemId) {
        borrowItemService.returnBook(itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get borrow items by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved borrow items"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public Page<BorrowItemResponseDto> getByUserId(
            @PathVariable Long userId, Pageable pageable) {
        return borrowItemService.getByUserId(userId, pageable);
    }

    @Operation(summary = "Get borrow items by book ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved borrow items"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/book/{bookId}")
    public Page<BorrowItemResponseDto> getByBookId(@PathVariable Long bookId, Pageable pageable) {
        return borrowItemService.getByBookId(bookId, pageable);
    }

    @Operation(summary = "Get overdue borrow items")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved overdue items")
    @GetMapping("/overdue")
    public Page<BorrowItemResponseDto> getOverdueItems(Pageable pageable) {
        return borrowItemService.getOverdueItems(pageable);
    }

    @Operation(summary = "Get borrow items by date range")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved borrow items")
    @GetMapping("/date-range")
    public Page<BorrowItemResponseDto> getByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        return borrowItemService.getBorrowItemsByDateRange(startDate, endDate, pageable);
    }

    @Operation(summary = "Get active borrow items for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active borrow items"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/active")
    public List<BorrowItemResponseDto> getActiveItemsByUser(@PathVariable Long userId) {
        return borrowItemService.getActiveItemsByUser(userId);
    }

    @Operation(summary = "Check if user has overdue items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked overdue status"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/exist-overdue")
    public ResponseEntity<Boolean> existsOverdueItemsByUserId(@PathVariable Long userId) {
        boolean hasOverdue = borrowItemService.existsOverdueItemsByUserId(userId);
        return ResponseEntity.ok(hasOverdue);
    }

    @Operation(summary = "Count active borrow items for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully counted active items"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/count-active")
    public ResponseEntity<Integer> countActiveItemsByUser(@PathVariable Long userId) {
        int count = borrowItemService.countActiveItemsByUser(userId);
        return ResponseEntity.ok(count);
    }
}