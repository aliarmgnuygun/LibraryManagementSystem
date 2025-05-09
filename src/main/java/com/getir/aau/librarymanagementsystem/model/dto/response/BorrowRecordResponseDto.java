package com.getir.aau.librarymanagementsystem.model.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a response DTO for a borrow record for tracking borrowed items.
 */

public record BorrowRecordResponseDto(
        Long id,
        Long userId,
        String userFullName,
        LocalDate borrowDate,
        LocalDate dueDate,
        List<BorrowItemResponseDto> items
) {}