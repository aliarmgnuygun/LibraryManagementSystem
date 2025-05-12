package com.getir.aau.librarymanagementsystem.controller;

import com.getir.aau.librarymanagementsystem.exception.ExceptionResult;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemResponseDto;
import com.getir.aau.librarymanagementsystem.service.BorrowItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
            @ApiResponse(responseCode = "400", description = "Invalid item ID or book already returned", content = @Content(schema = @Schema(implementation = ExceptionResult.class))),
            @ApiResponse(responseCode = "404", description = "Borrow item not found")
    })
    @PutMapping("/{itemId}/return")
    public ResponseEntity<Void> returnBook(@PathVariable Long itemId, @RequestParam Long userId) {
        borrowItemService.returnBook(itemId,userId);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Get borrow items by user ID",
            parameters = {
                    @Parameter(name = "page", description = "Page number (zero-based)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Page size", example = "10", schema = @Schema(type = "integer", defaultValue = "10")),
                    @Parameter(name = "sort", description = "Sort field and direction (e.g. id,asc)", example = "id,asc", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved borrow items",
                            content = @Content(schema = @Schema(implementation = BorrowItemPageResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "User not found",content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
            }
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<BorrowItemPageResponseDto> getByUserId(
            @PathVariable Long userId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(borrowItemService.getByUserId(userId, pageable));
    }

    @Operation(
            summary = "Get borrow items by book ID",
            parameters = {
                    @Parameter(name = "page", description = "Page number (zero-based)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Page size", example = "10", schema = @Schema(type = "integer", defaultValue = "10")),
                    @Parameter(name = "sort", description = "Sort field and direction (e.g. id,asc)", example = "id,asc", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved borrow items",
                            content = @Content(schema = @Schema(implementation = BorrowItemPageResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "Book not found",content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
            }
    )
    @GetMapping("/book/{bookId}")
    public ResponseEntity<BorrowItemPageResponseDto> getByBookId(@PathVariable Long bookId, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(borrowItemService.getByBookId(bookId, pageable));
    }

    @Operation(
            summary = "Get overdue borrow items",
            parameters = {
                    @Parameter(name = "page", description = "Page number (zero-based)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Page size", example = "10", schema = @Schema(type = "integer", defaultValue = "10")),
                    @Parameter(name = "sort", description = "Sort field and direction (e.g. id,asc)", example = "id,asc", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved overdue items",
                            content = @Content(schema = @Schema(implementation = BorrowItemPageResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "No overdue items found",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
            }
    )
    @GetMapping("/overdue")
    public ResponseEntity<BorrowItemPageResponseDto> getOverdueItems(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(borrowItemService.getOverdueItems(pageable));
    }

    @Operation(
            summary = "Get borrow items by date range",
            parameters = {
                    @Parameter(name = "startDate", description = "Start date (yyyy-MM-dd)", example = "2024-01-01", schema = @Schema(type = "string")),
                    @Parameter(name = "endDate", description = "End date (yyyy-MM-dd)", example = "2024-12-31", schema = @Schema(type = "string")),
                    @Parameter(name = "page", description = "Page number (zero-based)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Page size", example = "10", schema = @Schema(type = "integer", defaultValue = "10")),
                    @Parameter(name = "sort", description = "Sort field and direction (e.g. id,asc)", example = "id,asc", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved borrow items",
                            content = @Content(schema = @Schema(implementation = BorrowItemPageResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid date range",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))),
                    @ApiResponse(responseCode = "404", description = "No borrow items found for the given date range",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
            }
    )
    @GetMapping("/date-range")
    public ResponseEntity<BorrowItemPageResponseDto> getByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(borrowItemService.getBorrowItemsByDateRange(startDate, endDate, pageable));
    }

    @Operation(summary = "Get active borrow items for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active borrow items"),
            @ApiResponse(responseCode = "404", description = "No active borrow items found for the user",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
    })
    @GetMapping("/user/{userId}/active")
    public List<BorrowItemResponseDto> getActiveItemsByUser(@PathVariable Long userId) {
        return borrowItemService.getActiveItemsByUser(userId);
    }

    @Operation(summary = "Check if user has overdue items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked overdue status"),
            @ApiResponse(responseCode = "404", description = "No overdue items found for the user",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
    })
    @GetMapping("/user/{userId}/exist-overdue")
    public ResponseEntity<Boolean> existsOverdueItemsByUserId(@PathVariable Long userId) {
        boolean hasOverdue = borrowItemService.existsOverdueItemsByUserId(userId);
        return ResponseEntity.ok(hasOverdue);
    }

    @Operation(summary = "Count active borrow items for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully counted active items"),
            @ApiResponse(responseCode = "404", description = "No active items found for the user",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
    })
    @GetMapping("/user/{userId}/count-active")
    public ResponseEntity<Integer> countActiveItemsByUser(@PathVariable Long userId) {
        int count = borrowItemService.countActiveItemsByUser(userId);
        return ResponseEntity.ok(count);
    }
}