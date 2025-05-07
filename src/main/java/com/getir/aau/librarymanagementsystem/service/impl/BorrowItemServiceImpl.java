package com.getir.aau.librarymanagementsystem.service.impl;

import com.getir.aau.librarymanagementsystem.model.dto.BorrowItemResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Book;
import com.getir.aau.librarymanagementsystem.model.entity.BorrowItem;
import com.getir.aau.librarymanagementsystem.model.mapper.BorrowMapper;
import com.getir.aau.librarymanagementsystem.repository.BookRepository;
import com.getir.aau.librarymanagementsystem.repository.BorrowItemRepository;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.service.BorrowItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BorrowItemServiceImpl implements BorrowItemService {

    private final BorrowItemRepository borrowItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowMapper borrowMapper;

    @Override
    public void returnBook(Long itemId) {
        BorrowItem borrowItem = borrowItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Borrow item not found with id: " + itemId));

        if (borrowItem.isReturned()) {
            log.warn("Attempted to return already returned book item ID: {}", itemId);
            throw new IllegalStateException("Book already returned");
        }

        borrowItem.markAsReturned();

        Book book = borrowItem.getBook();
        book.returnBook();
        bookRepository.save(book);

        borrowItemRepository.save(borrowItem);

        log.info("Book with ID {} returned successfully by user ID {}",
                borrowItem.getBook().getId(), borrowItem.getUser().getId());
    }

    @Override
    public Page<BorrowItemResponseDto> getByUserId(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            log.warn("Attempted to retrieve borrow items for non-existent user ID: {}", userId);
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        log.debug("Retrieving borrow items for user ID: {}", userId);
        return borrowItemRepository.findByUserId(userId, pageable)
                .map(borrowMapper::toItemDto);
    }

    @Override
    public Page<BorrowItemResponseDto> getByBookId(Long bookId, Pageable pageable) {
        if (!bookRepository.existsById(bookId)) {
            log.warn("Attempted to retrieve borrow items for non-existent book ID: {}", bookId);
            throw new IllegalArgumentException("Book not found with id: " + bookId);
        }

        log.debug("Retrieving borrow items for book ID: {}", bookId);
        return borrowItemRepository.findByBookId(bookId, pageable)
                .map(borrowMapper::toItemDto);
    }

    @Override
    public Page<BorrowItemResponseDto> getOverdueItems(Pageable pageable) {
        log.info("Retrieving overdue items, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return borrowItemRepository.findOverdueItemsPageable(pageable)
                .map(borrowMapper::toItemDto);
    }

    @Override
    public Page<BorrowItemResponseDto> getBorrowItemsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }

        if (endDate == null) {
            endDate = LocalDate.now();
        }

        if (startDate.isAfter(endDate)) {
            log.warn("Invalid date range: startDate {} is after endDate {}", startDate, endDate);
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        log.info("Retrieving borrow items between {} and {}", startDate, endDate);
        return borrowItemRepository.findByBorrowDateBetween(startDate, endDate, pageable)
                .map(borrowMapper::toItemDto);
    }

    @Override
    public List<BorrowItemResponseDto> getActiveItemsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("Attempted to retrieve active items for non-existent user ID: {}", userId);
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        List<BorrowItem> activeItems = borrowItemRepository.findByUserIdAndReturnedFalse(userId);
        log.debug("Found {} active borrow items for user ID: {}", activeItems.size(), userId);

        return borrowMapper.toItemDtoList(activeItems);
    }

    @Override
    public boolean existsOverdueItemsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("Attempted to check overdue items for non-existent user ID: {}", userId);
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        List<BorrowItem> activeItems = borrowItemRepository.findByUserIdAndReturnedFalse(userId);
        LocalDate today = LocalDate.now();

        boolean hasOverdue = activeItems.stream()
                .anyMatch(item -> item.getDueDate().isBefore(today));

        log.debug("User ID: {} has overdue items: {}", userId, hasOverdue);
        return hasOverdue;
    }

    @Override
    public int countActiveItemsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("Attempted to count active items for non-existent user ID: {}", userId);
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        int count = borrowItemRepository.countActiveByUserId(userId);
        log.debug("User ID: {} has {} active borrow items", userId, count);
        return count;
    }
}