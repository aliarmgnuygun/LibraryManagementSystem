package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.dto.BorrowRecordRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.BorrowRecordResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BorrowRecordService {

    BorrowRecordResponseDto borrowBooks(BorrowRecordRequestDto dto);
    BorrowRecordResponseDto getById(Long id);

    Page<BorrowRecordResponseDto> getByUser(Long userId, Pageable pageable);
    Page<BorrowRecordResponseDto> getAll(Pageable pageable);
    Page<BorrowRecordResponseDto> filter(String email, LocalDate startDate, LocalDate endDate, Pageable pageable);

    void checkBorrowEligibility(Long userId);
    boolean isBookAvailableForBorrowing(Long bookId);
    List<BorrowRecordResponseDto> getActiveRecordsByUser(Long userId);
}