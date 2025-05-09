package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.dto.response.BookPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.request.BookRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookResponseDto;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookResponseDto create(BookRequestDto bookRequestDto);
    BookResponseDto update(Long id, BookRequestDto bookRequestDto);

    void delete(Long id);

    BookResponseDto getById(Long id);
    BookResponseDto getByIsbn(String isbn);

    BookPageResponseDto getByTitle(String title, Pageable pageable);
    BookPageResponseDto getByAuthorId(Long authorId, Pageable pageable);
    BookPageResponseDto getByAuthorName(String authorName, Pageable pageable);
    BookPageResponseDto getByCategoryId(Long categoryId, Pageable pageable);
    BookPageResponseDto getByGenre(String genre, Pageable pageable);
    BookPageResponseDto getAvailable(Pageable pageable);
    BookPageResponseDto getUnavailable(Pageable pageable);
    BookPageResponseDto searchByKeywords(String searchTerm, Pageable pageable);

    Long countBooksByAuthor(Long authorId);
}