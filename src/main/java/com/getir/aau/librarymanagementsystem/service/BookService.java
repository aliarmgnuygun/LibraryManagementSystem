package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.dto.BookResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    // BookResponseDto createBook(BookRequestDto bookRequestDto);
    // BookResponseDto updateBook(Long id, BookRequestDto bookRequestDto);

    void deleteBook(Long id);

    BookResponseDto getBookById(Long id);
    BookResponseDto getBookByIsbn(String isbn);

    Page<BookResponseDto> getBooksByTitle(String title, Pageable pageable);
    Page<BookResponseDto> getBooksByAuthorId(Long authorId, Pageable pageable);
    Page<BookResponseDto> getBooksByAuthorName(String authorName, Pageable pageable);
    Page<BookResponseDto> getBooksByCategoryId(Long categoryId, Pageable pageable);
    Page<BookResponseDto> getBooksByGenre(String genre, Pageable pageable);
    Page<BookResponseDto> getAvailableBooks(Pageable pageable);
    Page<BookResponseDto> searchBooks(String searchTerm, Pageable pageable);
    Long countBooksByAuthor(Long authorId);
}