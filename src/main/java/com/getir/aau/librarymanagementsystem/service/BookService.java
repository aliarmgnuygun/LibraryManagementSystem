package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.dto.BookPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.BookRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.BookResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookResponseDto createBook(BookRequestDto bookRequestDto);
    BookResponseDto updateBook(Long id, BookRequestDto bookRequestDto);

    void deleteBook(Long id);

    BookResponseDto getBookById(Long id);
    BookResponseDto getBookByIsbn(String isbn);

    BookPageResponseDto getBooksByTitle(String title, Pageable pageable);
    BookPageResponseDto getBooksByAuthorId(Long authorId, Pageable pageable);
    BookPageResponseDto getBooksByAuthorName(String authorName, Pageable pageable);
    BookPageResponseDto getBooksByCategoryId(Long categoryId, Pageable pageable);
    BookPageResponseDto getBooksByGenre(String genre, Pageable pageable);
    BookPageResponseDto getAvailableBooks(Pageable pageable);
    BookPageResponseDto searchBooks(String searchTerm, Pageable pageable);

    Long countBooksByAuthor(Long authorId);
}