package com.getir.aau.librarymanagementsystem.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Represents a request to borrow items with bookId from the library
 * */

public record BorrowRecordRequestDto(

        @NotNull(message = "Borrower ID is required")
        Long userId,

        @NotEmpty(message = "At least one item is required")
        List<@Valid BorrowItemRequestDto> items
) {}