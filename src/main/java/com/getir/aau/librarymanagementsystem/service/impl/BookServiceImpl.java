package com.getir.aau.librarymanagementsystem.service.impl;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.request.BookRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Author;
import com.getir.aau.librarymanagementsystem.model.entity.Book;
import com.getir.aau.librarymanagementsystem.model.entity.Category;
import com.getir.aau.librarymanagementsystem.model.mapper.BookMapper;
import com.getir.aau.librarymanagementsystem.repository.AuthorRepository;
import com.getir.aau.librarymanagementsystem.repository.BookRepository;
import com.getir.aau.librarymanagementsystem.repository.CategoryRepository;
import com.getir.aau.librarymanagementsystem.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;

    @Override
    public BookResponseDto create(BookRequestDto dto) {
        log.info("Creating book with title: {}", dto.title());

        if (bookRepository.existsByIsbnIgnoreCase(dto.isbn())) {
            log.warn("Book with ISBN {} already exists", dto.isbn());
            throw new ResourceAlreadyExistsException("Book", "isbn", dto.isbn());
        }

        Author author = authorRepository.findById(dto.authorId())
                .orElseThrow(() -> {
                    log.error("Author not found with ID: {}", dto.authorId());
                    return new ResourceNotFoundException("Author", "id", dto.authorId());
                });

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> {
                    log.error("Category not found with ID: {}", dto.categoryId());
                    return new ResourceNotFoundException("Category", "id", dto.categoryId());
                });

        Book book = bookMapper.toEntity(dto, author, category);
        Book saved = bookRepository.save(book);

        log.info("Book created successfully with ID: {}", saved.getId());
        return bookMapper.toDto(saved);
    }

    @Override
    public BookResponseDto update(Long id, BookRequestDto dto) {
        log.info("Updating book with ID: {}", id);

        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));

        Author author = authorRepository.findById(dto.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", dto.authorId()));
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.categoryId()));

        Book updated = Book.builder()
                .id(existing.getId())
                .title(dto.title())
                .isbn(dto.isbn())
                .description(dto.description())
                .publicationDate(dto.publicationDate())
                .genre(dto.genre())
                .numberOfCopies(dto.numberOfCopies())
                .available(dto.numberOfCopies() > 0)
                .author(author)
                .category(category)
                .build();

        Book saved = bookRepository.save(updated);
        log.info("Book updated successfully with ID: {}", saved.getId());
        return bookMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        log.warn("Deleting book with ID: {}", id);
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book", "id", id);
        }
        bookRepository.deleteById(id);
        log.info("Book deleted with ID: {}", id);
    }

    @Override
    public BookResponseDto getById(Long id) {
        log.info("Fetching book by ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + id));
        return bookMapper.toDto(book);
    }

    @Override
    public BookResponseDto getByIsbn(String isbn) {
        log.info("Fetching book by ISBN: {}", isbn);
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ISBN: " + isbn));
        return bookMapper.toDto(book);
    }

    @Override
    public BookPageResponseDto getByTitle(String title, Pageable pageable) {
        log.info("Searching books by title: {}", title);
        Page<BookResponseDto> page = bookRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(bookMapper::toDto);
        return new BookPageResponseDto(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    public BookPageResponseDto getByAuthorId(Long authorId, Pageable pageable) {
        log.info("Searching books by author ID: {}", authorId);
        Page<BookResponseDto> page = bookRepository.findByAuthorId(authorId, pageable)
                .map(bookMapper::toDto);
        return new BookPageResponseDto(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    public BookPageResponseDto getByAuthorName(String authorName, Pageable pageable) {
        log.info("Searching books by author name: {}", authorName);
        Page<BookResponseDto> page = bookRepository.findByAuthorNameContainingIgnoreCase(authorName, pageable)
                .map(bookMapper::toDto);
        return new BookPageResponseDto(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    public BookPageResponseDto getByCategoryId(Long categoryId, Pageable pageable) {
        log.info("Searching books by category ID: {}", categoryId);
        Page<BookResponseDto> page = bookRepository.findByCategoryId(categoryId, pageable)
                .map(bookMapper::toDto);
        return new BookPageResponseDto(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    public BookPageResponseDto getByGenre(String genre, Pageable pageable) {
        log.info("Searching books by genre: {}", genre);
        Page<BookResponseDto> page = bookRepository.findByGenreContainingIgnoreCase(genre, pageable)
                .map(bookMapper::toDto);
        return new BookPageResponseDto(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    public BookPageResponseDto getAvailable(Pageable pageable) {
        log.info("Fetching available books");
        Page<BookResponseDto> page = bookRepository.findAvailable(pageable)
                .map(bookMapper::toDto);
        return new BookPageResponseDto(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    public BookPageResponseDto getUnavailable(Pageable pageable) {
        log.info("Fetching unavailable books");
        Page<BookResponseDto> page = bookRepository.findUnavailable(pageable)
                .map(bookMapper::toDto);
        return new BookPageResponseDto(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    public BookPageResponseDto searchByKeywords(String searchTerm, Pageable pageable) {
        log.info("Searching books with keyword: {}", searchTerm);
        Page<BookResponseDto> page = bookRepository.searchByKeywords(searchTerm, pageable)
                .map(bookMapper::toDto);
        return new BookPageResponseDto(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    @Override
    public Long countBooksByAuthor(Long authorId) {
        log.info("Counting books for author ID: {}", authorId);
        return bookRepository.countBooksByAuthorId(authorId);
    }
}