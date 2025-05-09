package com.getir.aau.librarymanagementsystem.repository;

import com.getir.aau.librarymanagementsystem.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @SuppressWarnings("NullableProblems")
    Optional<Book> findById(Long id);
    Optional<Book> findByIsbn(String isbn);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Book> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.author.id = :authorId")
    Page<Book> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.author a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    Page<Book> findByAuthorNameContainingIgnoreCase(@Param("authorName") String authorName, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.category.id = :categoryId")
    Page<Book> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))")
    Page<Book> findByGenreContainingIgnoreCase(@Param("genre") String genre, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.available = true")
    Page<Book> findAvailableBooks(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.available = true")
    Page<Book> findUnavailableBooks(Pageable pageable);

    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN b.author a LEFT JOIN b.category c WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.genre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Book> searchBooks(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.author.id = :authorId")
    Long countBooksByAuthorId(@Param("authorId") Long authorId);
}