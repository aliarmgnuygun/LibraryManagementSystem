package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowItemResponseDto;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BorrowItemService {

    void returnBook(Long itemId, Long userId);
    BorrowItemPageResponseDto getByUserId(Long userId, Pageable pageable);
    BorrowItemPageResponseDto getByBookId(Long bookId, Pageable pageable);
    BorrowItemPageResponseDto getOverdueItems(Pageable pageable);
    BorrowItemPageResponseDto getBorrowItemsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);


    List<BorrowItemResponseDto> getActiveItemsByUser(Long userId);
    boolean existsOverdueItemsByUserId(Long userId);
    int countActiveItemsByUser(Long userId);
}