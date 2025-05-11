package com.getir.aau.librarymanagementsystem.unit.service;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.BookRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Author;
import com.getir.aau.librarymanagementsystem.model.entity.Book;
import com.getir.aau.librarymanagementsystem.model.entity.Category;
import com.getir.aau.librarymanagementsystem.model.mapper.BookMapper;
import com.getir.aau.librarymanagementsystem.repository.AuthorRepository;
import com.getir.aau.librarymanagementsystem.repository.BookRepository;
import com.getir.aau.librarymanagementsystem.repository.CategoryRepository;
import com.getir.aau.librarymanagementsystem.service.impl.BookServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock private BookRepository bookRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Author author;
    private Category category;
    private Book book;
    private BookRequestDto requestDto;
    private BookResponseDto responseDto;

    @BeforeEach
    void setup() {
        author = Author.builder().id(1L).name("Author").build();
        category = Category.builder().id(1L).name("Category").build();
        book = Book.builder()
                .id(1L)
                .title("Book Title")
                .isbn("1234567890")
                .author(author)
                .category(category)
                .available(true)
                .build();
        requestDto = new BookRequestDto(
                "Book Title", 1L, "1234567890", 1L,
                "desc",LocalDate.now(),"Fiction",5
        );
        responseDto = new BookResponseDto(1L, "Book Title", "1234567890", "desc",
                LocalDate.now(), "Fiction", 5, true, "Author", "Category");
    }

    @Nested
    @DisplayName("Create Method Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create book when ISBN does not exist")
        void shouldCreateBook() {
            given(bookRepository.existsByIsbnIgnoreCase(anyString())).willReturn(false);
            given(authorRepository.findById(1L)).willReturn(Optional.of(author));
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(bookMapper.toEntity(any(), eq(author), eq(category))).willReturn(book);
            given(bookRepository.save(any())).willReturn(book);
            given(bookMapper.toDto(any())).willReturn(responseDto);

            BookResponseDto result = bookService.create(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("Book Title");
        }

        @Test
        @DisplayName("Should throw exception when ISBN already exists")
        void shouldThrowWhenIsbnExists() {
            given(bookRepository.existsByIsbnIgnoreCase(anyString())).willReturn(true);

            assertThrows(ResourceAlreadyExistsException.class, () -> bookService.create(requestDto));
        }

        @Test
        @DisplayName("Should throw when author not found")
        void shouldThrowWhenAuthorNotFound() {
            given(bookRepository.existsByIsbnIgnoreCase(anyString())).willReturn(false);
            given(authorRepository.findById(1L)).willReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> bookService.create(requestDto));
        }

        @Test
        @DisplayName("Should throw when category not found")
        void shouldThrowWhenCategoryNotFound() {
            given(bookRepository.existsByIsbnIgnoreCase(anyString())).willReturn(false);
            given(authorRepository.findById(1L)).willReturn(Optional.of(author));
            given(categoryRepository.findById(1L)).willReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> bookService.create(requestDto));
        }
    }

    @Nested
    @DisplayName("Update Method Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update book successfully")
        void shouldUpdateBook() {
            given(bookRepository.findById(1L)).willReturn(Optional.of(book));
            given(authorRepository.findById(1L)).willReturn(Optional.of(author));
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(bookRepository.save(any())).willReturn(book);
            given(bookMapper.toDto(any())).willReturn(responseDto);

            BookResponseDto result = bookService.update(1L, requestDto);

            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("Book Title");
        }

        @Test
        @DisplayName("Should throw when book not found")
        void shouldThrowWhenBookNotFound() {
            given(bookRepository.findById(1L)).willReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> bookService.update(1L, requestDto));
        }

        @Test
        @DisplayName("Should throw when author not found on update")
        void shouldThrowWhenAuthorMissingOnUpdate() {
            given(bookRepository.findById(1L)).willReturn(Optional.of(book));
            given(authorRepository.findById(1L)).willReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> bookService.update(1L, requestDto));
        }

        @Test
        @DisplayName("Should throw when category not found on update")
        void shouldThrowWhenCategoryMissingOnUpdate() {
            given(bookRepository.findById(1L)).willReturn(Optional.of(book));
            given(authorRepository.findById(1L)).willReturn(Optional.of(author));
            given(categoryRepository.findById(1L)).willReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> bookService.update(1L, requestDto));
        }
    }
    @Nested
    @DisplayName("GetById Method Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return book when found by ID")
        void shouldReturnBookById() {
            given(bookRepository.findById(1L)).willReturn(Optional.of(book));
            given(bookMapper.toDto(book)).willReturn(responseDto);

            BookResponseDto result = bookService.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when book not found by ID")
        void shouldThrowWhenBookNotFoundById() {
            given(bookRepository.findById(1L)).willReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> bookService.getById(1L));
        }
    }

    @Nested
    @DisplayName("GetByIsbn Method Tests")
    class GetByIsbnTests {

        @Test
        @DisplayName("Should return book when found by ISBN")
        void shouldReturnBookByIsbn() {
            given(bookRepository.findByIsbn("1234567890")).willReturn(Optional.of(book));
            given(bookMapper.toDto(book)).willReturn(responseDto);

            BookResponseDto result = bookService.getByIsbn("1234567890");

            assertThat(result).isNotNull();
            assertThat(result.isbn()).isEqualTo("1234567890");
        }

        @Test
        @DisplayName("Should throw exception when book not found by ISBN")
        void shouldThrowWhenBookNotFoundByIsbn() {
            given(bookRepository.findByIsbn("invalid")).willReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> bookService.getByIsbn("invalid"));
        }
    }

    @Nested
    @DisplayName("Delete Method Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete book when found")
        void shouldDeleteBook() {
            given(bookRepository.existsById(1L)).willReturn(true);
            doNothing().when(bookRepository).deleteById(1L);

            bookService.delete(1L);

            verify(bookRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when book not found on delete")
        void shouldThrowWhenBookNotFoundOnDelete() {
            given(bookRepository.existsById(1L)).willReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> bookService.delete(1L));
        }
    }

    @Nested
    @DisplayName("GetByTitle Method Tests")
    class GetByTitleTests {

        @Test
        @DisplayName("Should return paginated books by title")
        void shouldReturnBooksByTitle() {
            Page<Book> bookPage = new PageImpl<>(List.of(book));
            given(bookRepository.findByTitleContainingIgnoreCase(eq("Book"), any()))
                    .willReturn(bookPage);
            given(bookMapper.toDto(book)).willReturn(responseDto);

            Pageable pageable = PageRequest.of(0, 10);
            BookPageResponseDto result = bookService.getByTitle("Book", pageable);

            assertThat(result).isNotNull();
            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().title()).isEqualTo("Book Title");
        }
    }

    @Nested
    @DisplayName("Search & Filter Method Tests")
    class SearchAndFilterTests {

        private Page<Book> bookPage;
        private Pageable pageable;

        @BeforeEach
        void setupPagination() {
            bookPage = new PageImpl<>(List.of(book));
            pageable = PageRequest.of(0, 10);
            given(bookMapper.toDto(book)).willReturn(responseDto);
        }

        @Test
        @DisplayName("Should return books by author ID")
        void shouldReturnBooksByAuthorId() {
            given(bookRepository.findByAuthorId(1L, pageable)).willReturn(bookPage);

            BookPageResponseDto result = bookService.getByAuthorId(1L, pageable);

            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().authorName()).isEqualTo("Author");
        }

        @Test
        @DisplayName("Should return books by author name")
        void shouldReturnBooksByAuthorName() {
            given(bookRepository.findByAuthorNameContainingIgnoreCase("auth", pageable)).willReturn(bookPage);

            BookPageResponseDto result = bookService.getByAuthorName("auth", pageable);

            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().authorName()).isEqualTo("Author");
        }

        @Test
        @DisplayName("Should return books by category ID")
        void shouldReturnBooksByCategoryId() {
            given(bookRepository.findByCategoryId(1L, pageable)).willReturn(bookPage);

            BookPageResponseDto result = bookService.getByCategoryId(1L, pageable);

            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().categoryName()).isEqualTo("Category");
        }

        @Test
        @DisplayName("Should return books by genre")
        void shouldReturnBooksByGenre() {
            given(bookRepository.findByGenreContainingIgnoreCase("fiction", pageable)).willReturn(bookPage);

            BookPageResponseDto result = bookService.getByGenre("fiction", pageable);

            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().genre()).isEqualTo("Fiction");
        }

        @Test
        @DisplayName("Should return available books")
        void shouldReturnAvailableBooks() {
            given(bookRepository.findAvailable(pageable)).willReturn(bookPage);

            BookPageResponseDto result = bookService.getAvailable(pageable);

            assertThat(result.books()).hasSize(1);
            assertThat(result.books().getFirst().available()).isTrue();
        }

        @Test
        @DisplayName("Should return unavailable books")
        void shouldReturnUnavailableBooks() {
            given(bookRepository.findUnavailable(pageable)).willReturn(bookPage);

            BookPageResponseDto result = bookService.getUnavailable(pageable);

            assertThat(result.books()).hasSize(1);
        }

        @Test
        @DisplayName("Should search books by keyword")
        void shouldSearchBooksByKeywords() {
            given(bookRepository.searchByKeywords("search", pageable)).willReturn(bookPage);

            BookPageResponseDto result = bookService.searchByKeywords("search", pageable);

            assertThat(result.books()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Count Method Tests")
    class CountTests {

        @Test
        @DisplayName("Should return book count by author ID")
        void shouldCountBooksByAuthor() {
            given(bookRepository.countBooksByAuthorId(1L)).willReturn(5L);

            Long count = bookService.countBooksByAuthor(1L);

            assertThat(count).isEqualTo(5L);
        }
    }
}