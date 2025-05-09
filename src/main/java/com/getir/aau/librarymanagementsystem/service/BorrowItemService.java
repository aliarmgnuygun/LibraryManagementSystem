package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BorrowItemService {

    void returnBook(Long itemId);
    Page<BorrowItemResponseDto> getByUserId(Long userId, Pageable pageable);
    Page<BorrowItemResponseDto> getByBookId(Long bookId, Pageable pageable);
    Page<BorrowItemResponseDto> getOverdueItems(Pageable pageable);
    Page<BorrowItemResponseDto> getBorrowItemsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<BorrowItemResponseDto> getActiveItemsByUser(Long userId);
    boolean existsOverdueItemsByUserId(Long userId);
    int countActiveItemsByUser(Long userId);
}