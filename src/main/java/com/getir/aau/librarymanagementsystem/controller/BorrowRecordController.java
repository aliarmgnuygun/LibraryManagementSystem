    package com.getir.aau.librarymanagementsystem.controller;

    import com.getir.aau.librarymanagementsystem.exception.ExceptionResult;
    import com.getir.aau.librarymanagementsystem.model.dto.request.BorrowRecordRequestDto;
    import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowRecordPageResponseDto;
    import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowRecordResponseDto;
    import com.getir.aau.librarymanagementsystem.service.BorrowRecordService;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.Parameter;
    import io.swagger.v3.oas.annotations.media.Content;
    import io.swagger.v3.oas.annotations.media.Schema;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springdoc.core.annotations.ParameterObject;
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
    @Tag(name = "Borrow Records", description = "Manage borrowing records of users")
    public class BorrowRecordController {

        private final BorrowRecordService borrowRecordService;

        @Operation(summary = "Create a new borrow record",
                responses = {
                        @ApiResponse(responseCode = "201", description = "Books borrowed successfully",
                                content = @Content(schema = @Schema(implementation = BorrowRecordResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input or unavailable books",
                                content = @Content(schema = @Schema(implementation = ExceptionResult.class))),
                        @ApiResponse(responseCode = "403", description = "User not eligible to borrow",
                                content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
                })
        @PostMapping
        public ResponseEntity<BorrowRecordResponseDto> borrowBooks(@Valid @RequestBody BorrowRecordRequestDto dto) {
            return new ResponseEntity<>(borrowRecordService.borrowBooks(dto), HttpStatus.CREATED);
        }

        @Operation(summary = "Get borrow record by ID",
                responses = {
                        @ApiResponse(responseCode = "200", description = "Borrow record found",
                                content = @Content(schema = @Schema(implementation = BorrowRecordResponseDto.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied: only the owner or librarian can view the records",
                                content = @Content(schema = @Schema(implementation = ExceptionResult.class))),
                        @ApiResponse(responseCode = "404", description = "Borrow record not found",
                                content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
                })
        @GetMapping("/{id}")
        public ResponseEntity<BorrowRecordResponseDto> getById(@PathVariable Long id) {
            return ResponseEntity.ok(borrowRecordService.getById(id));
        }

        @Operation(summary = "Get all borrow records (paginated)",
                parameters = {
                        @Parameter(name = "page", description = "Page number (zero-based)", example = "0"),
                        @Parameter(name = "size", description = "Page size", example = "10"),
                        @Parameter(name = "sort", description = "Sort field and direction", example = "id,asc")
                },
                responses = {
                        @ApiResponse(responseCode = "200", description = "List of all borrow records",
                                content = @Content(schema = @Schema(implementation = BorrowRecordResponseDto.class)))
                })
        @GetMapping
        public ResponseEntity<BorrowRecordPageResponseDto> getAll(@ParameterObject Pageable pageable) {
            return ResponseEntity.ok(borrowRecordService.getAll(pageable));
        }

        @Operation(summary = "Get borrow records by user ID",
                parameters = {
                        @Parameter(name = "page", description = "Page number (zero-based)", example = "0"),
                        @Parameter(name = "size", description = "Page size", example = "10"),
                        @Parameter(name = "sort", description = "Sort field and direction", example = "id,asc")
                },
                responses = {
                        @ApiResponse(responseCode = "200", description = "List of borrow records for the user",
                                content = @Content(schema = @Schema(implementation = BorrowRecordResponseDto.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied: only the owner or librarian can view the borrow records",
                                content = @Content(schema = @Schema(implementation = ExceptionResult.class))),
                        @ApiResponse(responseCode = "404", description = "User not found",
                                content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
                })
        @GetMapping("/user/{userId}")
        public ResponseEntity<BorrowRecordPageResponseDto> getByUser(@PathVariable Long userId, @ParameterObject Pageable pageable) {
            return ResponseEntity.ok(borrowRecordService.getByUser(userId, pageable));
        }

        @Operation(summary = "Get active borrow records by user ID",
                responses = {
                        @ApiResponse(responseCode = "200", description = "Active borrow records found",
                                content = @Content(schema = @Schema(implementation = BorrowRecordResponseDto.class))),
                        @ApiResponse(responseCode = "403", description = "Access denied: only the owner or librarian can view the borrow records",
                                content = @Content(schema = @Schema(implementation = ExceptionResult.class))),
                        @ApiResponse(responseCode = "404", description = "User not found",
                                content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
                })
        @GetMapping("/user/{userId}/active")
        public ResponseEntity<List<BorrowRecordResponseDto>> getActiveByUser(@PathVariable Long userId) {
            return ResponseEntity.ok(borrowRecordService.getActiveRecordsByUser(userId));
        }

        @Operation(summary = "Filter borrow records by email and date range",
                parameters = {
                        @Parameter(name = "page", description = "Page number (zero-based)", example = "0"),
                        @Parameter(name = "size", description = "Page size", example = "10"),
                        @Parameter(name = "sort", description = "Sort field and direction", example = "id,asc")
                },
                responses = {
                        @ApiResponse(responseCode = "200", description = "Filtered results",
                                content = @Content(schema = @Schema(implementation = BorrowRecordResponseDto.class)))
                })
        @GetMapping("/filter")
        public ResponseEntity<BorrowRecordPageResponseDto> filter(
                @RequestParam(required = false) String email,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                @ParameterObject Pageable pageable) {
            return ResponseEntity.ok(borrowRecordService.filter(email, startDate, endDate, pageable));
        }

        @Operation(summary = "Check if user is eligible to borrow books",
                responses = {
                        @ApiResponse(responseCode = "200", description = "User is eligible"),
                        @ApiResponse(responseCode = "403", description = "User is not eligible",
                                content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
                })
        @GetMapping("/check-eligibility/{userId}")
        public ResponseEntity<Void> checkEligibility(@PathVariable Long userId) {
            borrowRecordService.checkBorrowEligibility(userId);
            return ResponseEntity.ok().build();
        }


        @Operation(summary = "Check if a book is available for borrowing",
                responses = {
                        @ApiResponse(responseCode = "200", description = "Book is available"),
                        @ApiResponse(responseCode = "409", description = "Book is not available")
                })
        @GetMapping("/book-availability/{bookId}")
        public ResponseEntity<Void> isBookAvailable(@PathVariable Long bookId) {
            boolean available = borrowRecordService.isBookAvailableForBorrowing(bookId);
            return available ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }