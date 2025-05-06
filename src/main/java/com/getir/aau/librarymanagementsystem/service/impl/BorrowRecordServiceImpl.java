package com.getir.aau.librarymanagementsystem.service.impl;

import com.getir.aau.librarymanagementsystem.model.dto.BorrowItemRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.BorrowRecordRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.BorrowRecordResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.*;
import com.getir.aau.librarymanagementsystem.model.mapper.BorrowMapper;
import com.getir.aau.librarymanagementsystem.repository.BookRepository;
import com.getir.aau.librarymanagementsystem.repository.BorrowItemRepository;
import com.getir.aau.librarymanagementsystem.repository.BorrowRecordRepository;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.service.BorrowRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BorrowRecordServiceImpl implements BorrowRecordService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BorrowItemRepository borrowItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowMapper borrowMapper;

    private static final int MAX_BORROW_LIMIT = 5;
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;

    @Override
    public BorrowRecordResponseDto borrowBooks(BorrowRecordRequestDto dto) {

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + dto.userId()));

        checkBorrowEligibility(user.getId());

        // Create borrow record
        BorrowRecord borrowRecord = BorrowRecord.builder()
                .user(user)
                .borrowDate(LocalDate.now())
                .build();

        log.info("Creating new borrow record for user ID: {}", user.getId());

        LocalDate dueDate = LocalDate.now().plusDays(DEFAULT_LOAN_PERIOD_DAYS);
        List<BorrowItem> items = new ArrayList<>();

        for (BorrowItemRequestDto itemDto : dto.items()) {
            Book book = bookRepository.findById(itemDto.bookId())
                    .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + itemDto.bookId()));

            if (!book.isAvailable()) {
                log.warn("Book with ID {} is not available for borrowing", book.getId());
                throw new IllegalStateException("Book is not available: " + book.getTitle());
            }

            book.borrow();

            BorrowItem borrowItem = BorrowItem.builder()
                    .user(user)
                    .book(book)
                    .borrowDate(borrowRecord.getBorrowDate())
                    .dueDate(dueDate)
                    .returned(false)
                    .build();

            borrowItem.setBorrowRecord(borrowRecord);
            items.add(borrowItem);

            log.debug("Added book ID: {} to borrow record", book.getId());
        }

        borrowRecord.getItems().addAll(items);

        bookRepository.saveAll(items.stream().map(BorrowItem::getBook).toList());
        borrowRecordRepository.save(borrowRecord);
        borrowItemRepository.saveAll(items);

        log.info("Successfully created borrow record ID: {} with {} items", borrowRecord.getId(), items.size());

        return borrowMapper.toRecordDto(borrowRecord);
    }

    @Override
    public BorrowRecordResponseDto getById(Long id) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Borrow record not found with id: " + id));

        log.debug("Retrieved borrow record with ID: {}", id);
        return borrowMapper.toRecordDto(borrowRecord);
    }


    @Override
    public Page<BorrowRecordResponseDto> getByUser(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            log.warn("Attempted to retrieve borrow records for non-existent user ID: {}", userId);
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        log.debug("Retrieving borrow records for user ID: {}", userId);
        return borrowRecordRepository.findByUserId(userId, pageable)
                .map(borrowMapper::toRecordDto);
    }

    @Override
    public Page<BorrowRecordResponseDto> getAll(Pageable pageable) {
        log.debug("Retrieving all borrow records, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return borrowRecordRepository.findAllOrderByBorrowDateDesc(pageable)
                .map(borrowMapper::toRecordDto);
    }

    @Override
    public Page<BorrowRecordResponseDto> filter(String email, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("Filtering borrow records with email: {}, startDate: {}, endDate: {}",
                email, startDate, endDate);
        return borrowRecordRepository.findBorrowRecordsWithFilters(email, startDate, endDate, pageable)
                .map(borrowMapper::toRecordDto);
    }

    @Override
    public void checkBorrowEligibility(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        try {
            checkUserEligibility(user);
        } catch (Exception e) {
            log.debug("User ID: {} is not eligible to borrow. Reason: {}", userId, e.getMessage());
        }
    }

    @Override
    public boolean isBookAvailableForBorrowing(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));

        boolean available = book.isAvailable();
        log.debug("Book ID: {} availability status: {}", bookId, available);
        return available;
    }

    @Override
    public List<BorrowRecordResponseDto> getActiveRecordsByUser(Long userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            log.warn("Attempted to retrieve active records for non-existent user ID: {}", userId);
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        List<BorrowRecord> records = borrowRecordRepository.findByUserId(userId, Pageable.unpaged()).getContent();

        List<BorrowRecord> activeRecords = records.stream()
                .filter(record -> record.getItems().stream().anyMatch(item -> !item.isReturned()))
                .toList();

        log.debug("Found {} active borrow records for user ID: {}", activeRecords.size(), userId);
        return borrowMapper.toRecordDtoList(activeRecords);
    }

    private void checkUserEligibility(User user) {
        if (!user.getRole().getName().equals(ERole.ROLE_USER)) {
            log.warn("User ID: {} with role {} attempted to borrow books", user.getId(), user.getRole().getName());
            throw new AccessDeniedException("Only regular users can borrow books.");
        }

        List<BorrowItem> activeItems = borrowItemRepository.findByUserIdAndReturnedFalse(user.getId());

        if (activeItems.size() >= MAX_BORROW_LIMIT) {
            log.warn("User ID: {} has reached maximum borrowing limit of {}", user.getId(), MAX_BORROW_LIMIT);
            throw new IllegalStateException("You have reached the maximum borrowing limit.");
        }

        boolean hasOverdue = activeItems.stream()
                .anyMatch(item -> item.getDueDate().isBefore(LocalDate.now()));

        if (hasOverdue) {
            log.warn("User ID: {} has overdue items and cannot borrow new books", user.getId());
            throw new IllegalStateException("You cannot borrow books until overdue items are returned.");
        }

        log.debug("User ID: {} is eligible to borrow books", user.getId());
    }
}