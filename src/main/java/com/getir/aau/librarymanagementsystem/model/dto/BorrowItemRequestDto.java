package com.getir.aau.librarymanagementsystem.model.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Representing a request to borrow an item(book) from the library.
 */
public record BorrowItemRequestDto(

        @NotNull(message = "Book ID is required")
        Long bookId
) {}