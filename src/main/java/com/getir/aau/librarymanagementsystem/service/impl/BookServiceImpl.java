package com.getir.aau.librarymanagementsystem.service.impl;

import com.getir.aau.librarymanagementsystem.model.dto.BookResponseDto;
import com.getir.aau.librarymanagementsystem.model.mapper.BookMapper;
import com.getir.aau.librarymanagementsystem.repository.BookRepository;
import com.getir.aau.librarymanagementsystem.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public BookResponseDto getBookById(Long id) {
        return bookMapper.toDto(bookRepository.findById(id).orElseThrow());
    }

    @Override
    public BookResponseDto getBookByIsbn(String isbn) {
        return bookMapper.toDto(bookRepository.findByIsbn(isbn).orElseThrow());
    }

    @Override
    public Page<BookResponseDto> getBooksByTitle(String title, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookResponseDto> getBooksByAuthorId(Long authorId, Pageable pageable) {
        return bookRepository.findByAuthorId(authorId, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookResponseDto> getBooksByAuthorName(String authorName, Pageable pageable) {
        return bookRepository.findByAuthorNameContainingIgnoreCase(authorName, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookResponseDto> getBooksByCategoryId(Long categoryId, Pageable pageable) {
        return bookRepository.findByCategoryId(categoryId, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookResponseDto> getBooksByGenre(String genre, Pageable pageable) {
        return bookRepository.findByGenreContainingIgnoreCase(genre, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookResponseDto> getAvailableBooks(Pageable pageable) {
        return bookRepository.findAvailableBooks(pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookResponseDto> searchBooks(String searchTerm, Pageable pageable) {
        return bookRepository.searchBooks(searchTerm, pageable).map(bookMapper::toDto);
    }

    @Override
    public Long countBooksByAuthor(Long authorId) {
        return bookRepository.countBooksByAuthorId(authorId);
    }

    @Override
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}