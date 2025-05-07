package com.getir.aau.librarymanagementsystem.repository;

import com.getir.aau.librarymanagementsystem.model.entity.BorrowItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowItemRepository extends JpaRepository<BorrowItem, Long> {

    Page<BorrowItem> findByUserId(Long userId, Pageable pageable);
    List<BorrowItem> findByUserIdAndReturnedFalse(Long userId);
    Page<BorrowItem> findByBookId(Long bookId, Pageable pageable);

    @Query("""
        SELECT b FROM BorrowItem b
        WHERE b.returned = false AND b.dueDate < CURRENT_DATE
    """)
    Page<BorrowItem> findOverdueItemsPageable(Pageable pageable);

    @Query("""
        SELECT b FROM BorrowItem b
        WHERE b.borrowDate BETWEEN :startDate AND :endDate
    """)
    Page<BorrowItem> findByBorrowDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("""
    SELECT COUNT(b) FROM BorrowItem b
    WHERE b.user.id = :userId AND b.returned = false
""")
    int countActiveByUserId(@Param("userId") Long userId);

}