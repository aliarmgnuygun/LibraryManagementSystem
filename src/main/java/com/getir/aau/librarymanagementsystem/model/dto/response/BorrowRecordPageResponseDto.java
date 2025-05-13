package com.getir.aau.librarymanagementsystem.model.dto.response;

import java.util.List;

public record BorrowRecordPageResponseDto(
        List<BorrowRecordResponseDto> items,
        int totalPages,
        long totalItems
) {}