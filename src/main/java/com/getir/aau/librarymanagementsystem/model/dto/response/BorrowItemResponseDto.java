package com.getir.aau.librarymanagementsystem.model.dto.response;

import java.time.LocalDate;

/**
 * Represents a response DTO for a borrowed item(book) for tracking the borrowing process with date and status.
 */
public record BorrowItemResponseDto(
        Long id,
        Long bookId,
        String bookTitle,
        Long userId,
        String userEmail,
        LocalDate borrowDate,
        LocalDate dueDate,
        LocalDate returnDate,
        boolean returned
) {}