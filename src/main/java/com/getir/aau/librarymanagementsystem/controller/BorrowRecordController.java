package com.getir.aau.librarymanagementsystem.controller;

import com.getir.aau.librarymanagementsystem.model.dto.BorrowRecordRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.BorrowRecordResponseDto;
import com.getir.aau.librarymanagementsystem.service.BorrowRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/borrow-records")
@RequiredArgsConstructor
@Tag(name = "Borrow Records", description = "Borrowing operations for books")
public class BorrowRecordController {

    private final BorrowRecordService borrowRecordService;

    @Operation(summary = "Create a new borrow record", responses = {
            @ApiResponse(responseCode = "201", description = "Books borrowed successfully",
                    content = @Content(schema = @Schema(implementation = BorrowRecordResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or unavailable books"),
            @ApiResponse(responseCode = "403", description = "User not eligible to borrow")
    })
    @PostMapping
    public ResponseEntity<BorrowRecordResponseDto> borrowBooks(@Valid @RequestBody BorrowRecordRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(borrowRecordService.borrowBooks(dto));
    }

    @Operation(summary = "Get borrow record by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Borrow record found",
                    content = @Content(schema = @Schema(implementation = BorrowRecordResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Borrow record not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BorrowRecordResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(borrowRecordService.getById(id));
    }

    @Operation(summary = "Get all borrow records (paginated)", responses = {
            @ApiResponse(responseCode = "200", description = "List of all borrow records")
    })
    @GetMapping
    public ResponseEntity<Page<BorrowRecordResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(borrowRecordService.getAll(pageable));
    }

    @Operation(summary = "Get borrow records by user ID", responses = {
            @ApiResponse(responseCode = "200", description = "List of borrow records for the user"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<BorrowRecordResponseDto>> getByUser(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(borrowRecordService.getByUser(userId, pageable));
    }

    @Operation(summary = "Get active borrow records by user ID", responses = {
            @ApiResponse(responseCode = "200", description = "Active borrow records found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<BorrowRecordResponseDto>> getActiveByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(borrowRecordService.getActiveRecordsByUser(userId));
    }

    @Operation(summary = "Filter borrow records by email and date range", responses = {
            @ApiResponse(responseCode = "200", description = "Filtered results")
    })
    @GetMapping("/filter")
    public ResponseEntity<Page<BorrowRecordResponseDto>> filter(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        return ResponseEntity.ok(borrowRecordService.filter(email, startDate, endDate, pageable));
    }

    @Operation(summary = "Check if user is eligible to borrow books", responses = {
            @ApiResponse(responseCode = "200", description = "User is eligible"),
            @ApiResponse(responseCode = "403", description = "User is not eligible")
    })
    @GetMapping("/check-eligibility/{userId}")
    public ResponseEntity<Void> checkEligibility(@PathVariable Long userId) {
        try {
            borrowRecordService.checkBorrowEligibility(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "Check if a book is available for borrowing", responses = {
            @ApiResponse(responseCode = "200", description = "Book is available"),
            @ApiResponse(responseCode = "409", description = "Book is not available")
    })
    @GetMapping("/book-availability/{bookId}")
    public ResponseEntity<Void> isBookAvailable(@PathVariable Long bookId) {
        boolean available = borrowRecordService.isBookAvailableForBorrowing(bookId);
        return available ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}