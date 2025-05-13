package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.dto.request.BorrowRecordRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowRecordPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowRecordResponseDto;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BorrowRecordService {

    BorrowRecordResponseDto borrowBooks(BorrowRecordRequestDto dto);
    BorrowRecordResponseDto getById(Long id);

    BorrowRecordPageResponseDto getByUser(Long userId, Pageable pageable);
    BorrowRecordPageResponseDto getAll(Pageable pageable);
    BorrowRecordPageResponseDto filter(String email, LocalDate startDate, LocalDate endDate, Pageable pageable);

    void checkBorrowEligibility(Long userId);
    boolean isBookAvailableForBorrowing(Long bookId);
    List<BorrowRecordResponseDto> getActiveRecordsByUser(Long userId);
}