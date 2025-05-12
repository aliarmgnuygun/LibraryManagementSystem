package com.getir.aau.librarymanagementsystem.model.dto.response;

import java.util.List;

public record BorrowItemPageResponseDto(
        List<BorrowItemResponseDto> items,
        int totalPages,
        long totalItems
) {}