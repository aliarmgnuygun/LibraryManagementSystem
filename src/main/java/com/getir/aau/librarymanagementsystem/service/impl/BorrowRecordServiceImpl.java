package com.getir.aau.librarymanagementsystem.service.impl;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.BorrowItemRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.request.BorrowRecordRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BorrowRecordResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.*;
import com.getir.aau.librarymanagementsystem.model.mapper.BorrowMapper;
import com.getir.aau.librarymanagementsystem.repository.BookRepository;
import com.getir.aau.librarymanagementsystem.repository.BorrowItemRepository;
import com.getir.aau.librarymanagementsystem.repository.BorrowRecordRepository;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.security.SecurityUtils;
import com.getir.aau.librarymanagementsystem.service.BorrowRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private final SecurityUtils securityUtils;

    private static final int MAX_BORROW_LIMIT = 5;
    private static final int DEFAULT_LOAN_PERIOD_DAYS = 14;

    @Override
    public BorrowRecordResponseDto borrowBooks(BorrowRecordRequestDto dto) {

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", dto.userId());
                    return new ResourceNotFoundException("User", "id", dto.userId());
                });

        checkUserEligibility(user);

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(DEFAULT_LOAN_PERIOD_DAYS);

        // Create borrow record
        BorrowRecord borrowRecord = BorrowRecord.builder()
                .user(user)
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .build();

        log.info("Creating new borrow record for user ID: {}", user.getId());

        for (BorrowItemRequestDto itemDto : dto.items()) {
            Book book = bookRepository.findById(itemDto.bookId())
                    .orElseThrow(() -> {
                        log.warn("Book not found with id: {}", itemDto.bookId());
                        return new ResourceNotFoundException("Book", "id", itemDto.bookId());
                    });

            if (!book.isAvailable()) {
                log.warn("Book with ID {} is not available for borrowing", book.getId());
                throw new ResourceAlreadyExistsException("Book", "id", book.getId());
            }

            book.borrow();

            BorrowItem borrowItem = BorrowItem.builder()
                    .user(user)
                    .book(book)
                    .borrowDate(borrowRecord.getBorrowDate())
                    .dueDate(dueDate)
                    .returned(false)
                    .build();

            borrowRecord.addItem(borrowItem);
            log.debug("Added book ID: {} to borrow record", book.getId());
        }

        bookRepository.saveAll(borrowRecord.getItems().stream().map(BorrowItem::getBook).toList());
        borrowRecordRepository.save(borrowRecord);
        borrowItemRepository.saveAll(borrowRecord.getItems());

        log.info("Successfully created borrow record ID: {} with {} items", borrowRecord.getId(), borrowRecord.getItems().size());

        return borrowMapper.toRecordDto(borrowRecord);
    }

    @Override
    public BorrowRecordResponseDto getById(Long id) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Borrow record not found with id: {}", id);
                    return new ResourceNotFoundException("BorrowRecord", "id", id);
                });

        if (!securityUtils.isLibrarian()) {
            User currentUser = getCurrentUser();
            if (!borrowRecord.getUser().getId().equals(currentUser.getId())) {
                log.warn("Unauthorized access attempt to record ID: {} by user ID: {}", id, currentUser.getId());
                throw new AccessDeniedException("You are not allowed to access this record.");
            }
        }

        log.debug("Retrieved borrow record with ID: {}", id);
        return borrowMapper.toRecordDto(borrowRecord);
    }

    @Override
    public Page<BorrowRecordResponseDto> getByUser(Long userId, Pageable pageable) {
        User currentUser = getCurrentUser();
        if (!securityUtils.isLibrarian() && !userId.equals(currentUser.getId())) {
            throw new AccessDeniedException("You cannot view other users' borrow records.");
        }

        if (!userRepository.existsById(userId)) {
            log.warn("User not found while retrieving borrow records, id: {}", userId);
            throw new ResourceNotFoundException("User", "id", userId);
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
                .orElseThrow(() -> {
                    log.warn("User not found during eligibility check, id: {}", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });

        checkUserEligibility(user);
    }

    @Override
    public boolean isBookAvailableForBorrowing(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.warn("Book not found while checking availability, id: {}", bookId);
                    return new ResourceNotFoundException("Book", "id", bookId);
                });

        boolean available = book.isAvailable();
        log.debug("Book ID: {} availability status: {}", bookId, available);
        return available;
    }

    @Override
    public List<BorrowRecordResponseDto> getActiveRecordsByUser(Long userId) {
        User currentUser = getCurrentUser();
        if (!securityUtils.isLibrarian() && !userId.equals(currentUser.getId())) {
            throw new AccessDeniedException("You cannot view active records of other users.");
        }

        if (!userRepository.existsById(userId)) {
            log.warn("Attempted to retrieve active records for non-existent user ID: {}", userId);
            throw new ResourceNotFoundException("User", "id", userId);
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
    private User getCurrentUser() {
        String email = securityUtils.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}