package com.getir.aau.librarymanagementsystem.repository;

import com.getir.aau.librarymanagementsystem.model.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT br FROM BorrowRecord br ORDER BY br.borrowDate DESC")
    Page<BorrowRecord> findAllOrderByBorrowDateDesc(Pageable pageable);

    @Query("""
        SELECT br FROM BorrowRecord br
        JOIN br.user u
        WHERE (:email IS NULL OR u.email LIKE %:email%)
        AND (:startDate IS NULL OR br.borrowDate >= :startDate)
        AND (:endDate IS NULL OR br.borrowDate <= :endDate)
    """)
    Page<BorrowRecord> findBorrowRecordsWithFilters(
            @Param("email") String email,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
}