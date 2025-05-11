package com.getir.aau.librarymanagementsystem.integration.service;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.BookRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Author;
import com.getir.aau.librarymanagementsystem.model.entity.Book;
import com.getir.aau.librarymanagementsystem.model.entity.Category;
import com.getir.aau.librarymanagementsystem.repository.AuthorRepository;
import com.getir.aau.librarymanagementsystem.repository.BookRepository;
import com.getir.aau.librarymanagementsystem.repository.CategoryRepository;
import com.getir.aau.librarymanagementsystem.service.BookService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("BookService Integration Tests")
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private Author author;
    private Category category;
    private BookRequestDto requestDto;

    @BeforeEach
    void setup() {
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        authorRepository.deleteAll();

        author = authorRepository.save(Author.builder().name("Author").description("Test author description").build());
        category = categoryRepository.save(Category.builder().name("Category").build());

        requestDto = new BookRequestDto(
                "Book Title", author.getId(), "1234567890", category.getId(),
                "desc", LocalDate.now(), "Fiction", 5
        );
    }

    @Nested
    @DisplayName("Create Method Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create book successfully")
        void shouldCreateBook() {
            BookResponseDto result = bookService.create(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("Book Title");

            Book saved = bookRepository.findById(result.id()).orElse(null);
            assertThat(saved).isNotNull();
            assertThat(saved.getIsbn()).isEqualTo("1234567890");
        }

        @Test
        @DisplayName("Should throw when ISBN already exists")
        void shouldThrowWhenDuplicateIsbn() {
            bookService.create(requestDto);

            assertThrows(ResourceAlreadyExistsException.class, () ->
                    bookService.create(requestDto));
        }
    }

    @Nested
    @DisplayName("Update Method Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update book successfully")
        void shouldUpdateBook() {
            BookResponseDto created = bookService.create(requestDto);

            BookRequestDto updateDto = new BookRequestDto(
                    "Updated Title", author.getId(), "ISBN-NUMBER", category.getId(),
                    "Updated desc", LocalDate.now(), "Mystery", 3
            );

            BookResponseDto updated = bookService.update(created.id(), updateDto);

            assertThat(updated).isNotNull();
            assertThat(updated.title()).isEqualTo("Updated Title");
            assertThat(updated.genre()).isEqualTo("Mystery");
        }

        @Test
        @DisplayName("Should throw when updating non-existent book")
        void shouldThrowWhenUpdatingMissingBook() {
            assertThrows(ResourceNotFoundException.class, () ->
                    bookService.update(999L, requestDto));
        }
    }

    @Nested
    @DisplayName("GetById & GetByIsbn Method Tests")
    class GetTests {

        @Test
        @DisplayName("Should return book by ID")
        void shouldReturnBookById() {
            BookResponseDto created = bookService.create(requestDto);
            BookResponseDto found = bookService.getById(created.id());

            assertThat(found).isNotNull();
            assertThat(found.id()).isEqualTo(created.id());
        }

        @Test
        @DisplayName("Should return book by ISBN")
        void shouldReturnBookByIsbn() {
            BookResponseDto created = bookService.create(requestDto);
            BookResponseDto found = bookService.getByIsbn(created.isbn());

            assertThat(found).isNotNull();
            assertThat(found.isbn()).isEqualTo("1234567890");
        }
    }

    @Nested
    @DisplayName("Delete Method Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete existing book")
        void shouldDeleteBook() {
            BookResponseDto created = bookService.create(requestDto);

            bookService.delete(created.id());

            assertThat(bookRepository.existsById(created.id())).isFalse();
        }

        @Test
        @DisplayName("Should throw when deleting non-existent book")
        void shouldThrowWhenDeletingMissingBook() {
            assertThrows(ResourceNotFoundException.class, () ->
                    bookService.delete(999L));
        }
    }

    @Nested
    @DisplayName("Pagination and Search Method Tests")
    class PaginationAndSearchTests {

        private Pageable pageable;

        @BeforeEach
        void setupPagination() {
            pageable = PageRequest.of(0, 10);
            bookService.create(requestDto);
        }

        @Test
        @DisplayName("Should return books by title")
        void shouldReturnBooksByTitle() {
            BookPageResponseDto result = bookService.getByTitle("Book", pageable);

            assertThat(result).isNotNull();
            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().title()).contains("Book");
        }

        @Test
        @DisplayName("Should return books by author ID")
        void shouldReturnBooksByAuthorId() {
            BookPageResponseDto result = bookService.getByAuthorId(author.getId(), pageable);

            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().authorName()).isEqualTo("Author");
        }

        @Test
        @DisplayName("Should return books by author name")
        void shouldReturnBooksByAuthorName() {
            BookPageResponseDto result = bookService.getByAuthorName("auth", pageable);

            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().authorName()).contains("Author");
        }

        @Test
        @DisplayName("Should return books by category ID")
        void shouldReturnBooksByCategoryId() {
            BookPageResponseDto result = bookService.getByCategoryId(category.getId(), pageable);

            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().categoryName()).isEqualTo("Category");
        }

        @Test
        @DisplayName("Should return books by genre")
        void shouldReturnBooksByGenre() {
            BookPageResponseDto result = bookService.getByGenre("fic", pageable);

            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().genre()).contains("Fiction");
        }

        @Test
        @DisplayName("Should return available books")
        void shouldReturnAvailableBooks() {
            BookPageResponseDto result = bookService.getAvailable(pageable);

            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().available()).isTrue();
        }
        /*
        @Test
        @DisplayName("Should return unavailable books")
        void shouldReturnUnavailableBooks() {
            // Arrange
            BookRequestDto unavailableDto = new BookRequestDto(
                    "Unavailable Book",
                    author.getId(),
                    "9876543210123",
                    category.getId(),
                    "Out of stock book description",
                    LocalDate.now(),
                    "Drama",
                    0
            );
            bookService.create(unavailableDto);

            // Act
            BookPageResponseDto result = bookService.getUnavailable(pageable);

            // Assert
            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().numberOfCopies()).isEqualTo(0);
        }


        @Test
        @DisplayName("Should return books matching search keyword")
        void shouldSearchByKeyword() {
            BookPageResponseDto result = bookService.searchByKeywords("desc", pageable);

            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().description()).contains("desc");
        }
         */
    }

    @Nested
    @DisplayName("Count Method Test")
    class CountTests {

        @Test
        @DisplayName("Should return correct book count by author ID")
        void shouldCountBooksByAuthorId() {
            bookService.create(requestDto);

            Long count = bookService.countBooksByAuthor(author.getId());

            assertThat(count).isEqualTo(1L);
        }
    }
}