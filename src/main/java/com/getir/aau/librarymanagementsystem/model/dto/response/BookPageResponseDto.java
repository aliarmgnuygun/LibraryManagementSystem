package com.getir.aau.librarymanagementsystem.model.dto.response;

import java.util.List;

public record BookPageResponseDto(
        List<BookResponseDto> books,
        int totalPages,
        long totalItems
) {}