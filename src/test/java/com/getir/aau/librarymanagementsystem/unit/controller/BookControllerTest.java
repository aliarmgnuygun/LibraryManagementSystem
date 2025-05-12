package com.getir.aau.librarymanagementsystem.unit.controller;

import com.getir.aau.librarymanagementsystem.controller.BookController;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.BookRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookResponseDto;
import com.getir.aau.librarymanagementsystem.service.BookService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookController Unit Tests")
public class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private BookRequestDto bookRequestDto;
    private BookResponseDto bookResponseDto;
    private BookPageResponseDto pageResponseDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        bookRequestDto = new BookRequestDto(
                "Test Book",
                1L,
                "1234567890123",
                1L,
                "Test description",
                LocalDate.now(),
                "Fiction",
                100
        );

        bookResponseDto = new BookResponseDto(
                1L,
                "Test Book",
                "1234567890123",
                "Test description",
                LocalDate.now(),
                "Test genre",
                100,
                true,
                "Test Author",
                "Test Category"
        );

        List<BookResponseDto> bookResponseList = new ArrayList<>();
        bookResponseList.add(bookResponseDto);

        pageResponseDto = new BookPageResponseDto(bookResponseList, 1, 1);
    }

    @Nested
    @DisplayName("POST /api/books")
    class CreateTests {

        @Test
        @DisplayName("should return 201 CREATED when book creation is successful")
        void shouldReturn201CreatedWhenBookCreationIsSuccessful() {
            when(bookService.create(any(BookRequestDto.class))).thenReturn(bookResponseDto);

            ResponseEntity<BookResponseDto> response = bookController.create(bookRequestDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isEqualTo(bookResponseDto);
            verify(bookService).create(bookRequestDto);
        }

        @Test
        @DisplayName("should throw exception when ISBN already exists")
        void shouldThrowExceptionWhenIsbnAlreadyExists() {
            when(bookService.create(any(BookRequestDto.class)))
                    .thenThrow(new DataIntegrityViolationException("Book with ISBN already exists"));

            assertThrows(DataIntegrityViolationException.class, () -> bookController.create(bookRequestDto));
            verify(bookService).create(bookRequestDto);
        }
    }

    @Nested
    @DisplayName("PUT /api/books/{id}")
    class UpdateTests {

        @Test
        @DisplayName("should return 200 OK when book update is successful")
        void shouldReturn200OkWhenBookUpdateIsSuccessful() {
            when(bookService.update(anyLong(), any(BookRequestDto.class))).thenReturn(bookResponseDto);

            ResponseEntity<BookResponseDto> response = bookController.update(1L, bookRequestDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(bookResponseDto);
            verify(bookService).update(1L, bookRequestDto);
        }

        @Test
        @DisplayName("should throw exception when book to update does not exist")
        void shouldThrowExceptionWhenBookToUpdateDoesNotExist() {
            when(bookService.update(eq(999L), any(BookRequestDto.class)))
                    .thenThrow(new ResourceNotFoundException("Book", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> bookController.update(999L, bookRequestDto));
            verify(bookService).update(999L, bookRequestDto);
        }

        @Test
        @DisplayName("should throw exception when updated ISBN conflicts with another book")
        void shouldThrowExceptionWhenUpdatedIsbnConflictsWithAnotherBook() {
            when(bookService.update(anyLong(), any(BookRequestDto.class)))
                    .thenThrow(new DataIntegrityViolationException("ISBN conflicts with another book"));

            assertThrows(DataIntegrityViolationException.class, () -> bookController.update(1L, bookRequestDto));
            verify(bookService).update(1L, bookRequestDto);
        }
    }

    @Nested
    @DisplayName("DELETE /api/books/{id}")
    class DeleteTests {

        @Test
        @DisplayName("should return 204 NO CONTENT when book deletion is successful")
        void shouldReturn204NoContentWhenBookDeletionIsSuccessful() {
            doNothing().when(bookService).delete(anyLong());

            ResponseEntity<Void> response = bookController.delete(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(bookService).delete(1L);
        }

        @Test
        @DisplayName("should throw exception when book to delete does not exist")
        void shouldThrowExceptionWhenBookToDeleteDoesNotExist() {
            doThrow(new ResourceNotFoundException("Book", "id", 999L)).when(bookService).delete(999L);

            assertThrows(ResourceNotFoundException.class, () -> bookController.delete(999L));
            verify(bookService).delete(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/books/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("should return 200 OK with book when book is found")
        void shouldReturn200OkWithBookWhenBookIsFound() {
            when(bookService.getById(anyLong())).thenReturn(bookResponseDto);

            ResponseEntity<BookResponseDto> response = bookController.getById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(bookResponseDto);
            verify(bookService).getById(1L);
        }

        @Test
        @DisplayName("should throw exception when book is not found")
        void shouldThrowExceptionWhenBookIsNotFound() {
            when(bookService.getById(999L)).thenThrow(new ResourceNotFoundException("Book", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> bookController.getById(999L));
            verify(bookService).getById(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/books/search")
    class SearchByKeywordsTests {

        @Test
        @DisplayName("should return 200 OK with books matching keyword")
        void shouldReturn200OkWithBooksMatchingKeyword() {
            when(bookService.searchByKeywords(anyString(), any(Pageable.class))).thenReturn(pageResponseDto);

            ResponseEntity<BookPageResponseDto> response = bookController.searchByKeywords("test", pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(pageResponseDto);
            verify(bookService).searchByKeywords("test", pageable);
        }

        @Test
        @DisplayName("should return 200 OK with empty list when no books match keyword")
        void shouldReturn200OkWithEmptyListWhenNoBooksMatchKeyword() {
            BookPageResponseDto emptyPage = new BookPageResponseDto(Collections.emptyList(), 0, 0);
            when(bookService.searchByKeywords(anyString(), any(Pageable.class))).thenReturn(emptyPage);

            ResponseEntity<BookPageResponseDto> response = bookController.searchByKeywords("nonexistent", pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            Assertions.assertNotNull(response.getBody());
            assertThat(response.getBody().books()).isEmpty();
            verify(bookService).searchByKeywords("nonexistent", pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/books/category/{categoryId}")
    class GetByCategoryIdTests {

        @Test
        @DisplayName("should return 200 OK with books in category")
        void shouldReturn200OkWithBooksInCategory() {
            when(bookService.getByCategoryId(anyLong(), any(Pageable.class))).thenReturn(pageResponseDto);

            ResponseEntity<BookPageResponseDto> response = bookController.getByCategoryId(1L, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(pageResponseDto);
            verify(bookService).getByCategoryId(1L, pageable);
        }

        @Test
        @DisplayName("should throw exception when category is not found")
        void shouldThrowExceptionWhenCategoryIsNotFound() {
            when(bookService.getByCategoryId(eq(999L), any(Pageable.class)))
                    .thenThrow(new ResourceNotFoundException("Category", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> bookController.getByCategoryId(999L, pageable));
            verify(bookService).getByCategoryId(999L, pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/books/author/{authorId}")
    class GetByAuthorIdTests {

        @Test
        @DisplayName("should return 200 OK with author's books")
        void shouldReturn200OkWithAuthorBooks() {
            when(bookService.getByAuthorId(anyLong(), any(Pageable.class))).thenReturn(pageResponseDto);

            ResponseEntity<BookPageResponseDto> response = bookController.getByAuthorId(1L, pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(pageResponseDto);
            verify(bookService).getByAuthorId(1L, pageable);
        }

        @Test
        @DisplayName("should throw exception when author is not found")
        void shouldThrowExceptionWhenAuthorIsNotFound() {
            when(bookService.getByAuthorId(eq(999L), any(Pageable.class)))
                    .thenThrow(new ResourceNotFoundException("Author", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> bookController.getByAuthorId(999L, pageable));
            verify(bookService).getByAuthorId(999L, pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/books/available")
    class GetAvailableTests {

        @Test
        @DisplayName("should return 200 OK with available books")
        void shouldReturn200OkWithAvailableBooks() {
            when(bookService.getAvailable(any(Pageable.class))).thenReturn(pageResponseDto);

            ResponseEntity<BookPageResponseDto> response = bookController.getAvailable(pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(pageResponseDto);
            verify(bookService).getAvailable(pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/books/unavailable")
    class GetUnavailableTests {

        @Test
        @DisplayName("should return 200 OK with unavailable books")
        void shouldReturn200OkWithUnavailableBooks() {
            when(bookService.getUnavailable(any(Pageable.class))).thenReturn(pageResponseDto);

            ResponseEntity<BookPageResponseDto> response = bookController.getUnavailable(pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(pageResponseDto);
            verify(bookService).getUnavailable(pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/books/genre")
    class GetByGenreTests {

        @Test
        @DisplayName("should return 200 OK with books matching genre")
        void shouldReturn200OkWithBooksMatchingGenre() {
            when(bookService.getByGenre(anyString(), any(Pageable.class))).thenReturn(pageResponseDto);

            ResponseEntity<BookPageResponseDto> response = bookController.getByGenre("Fiction", pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(pageResponseDto);
            verify(bookService).getByGenre("Fiction", pageable);
        }

        @Test
        @DisplayName("should throw exception when genre parameter is invalid")
        void shouldThrowExceptionWhenGenreParameterIsInvalid() {
            when(bookService.getByGenre(eq(""), any(Pageable.class)))
                    .thenThrow(new IllegalArgumentException("Genre cannot be empty"));

            assertThrows(IllegalArgumentException.class, () -> bookController.getByGenre("", pageable));
            verify(bookService).getByGenre("", pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/books/author/name")
    class GetByAuthorNameTests {

        @Test
        @DisplayName("should return 200 OK with books by author name")
        void shouldReturn200OkWithBooksByAuthorName() {
            when(bookService.getByAuthorName(anyString(), any(Pageable.class))).thenReturn(pageResponseDto);

            ResponseEntity<BookPageResponseDto> response = bookController.getByAuthorName("Test Author", pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(pageResponseDto);
            verify(bookService).getByAuthorName("Test Author", pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/books/title")
    class GetByTitleTests {

        @Test
        @DisplayName("should return 200 OK with books matching title")
        void shouldReturn200OkWithBooksMatchingTitle() {
            when(bookService.getByTitle(anyString(), any(Pageable.class))).thenReturn(pageResponseDto);

            ResponseEntity<BookPageResponseDto> response = bookController.getByTitle("Test Book", pageable);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(pageResponseDto);
            verify(bookService).getByTitle("Test Book", pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/books/isbn/{isbn}")
    class GetByIsbnTests {

        @Test
        @DisplayName("should return 200 OK with book matching ISBN")
        void shouldReturn200OkWithBookMatchingIsbn() {
            when(bookService.getByIsbn(anyString())).thenReturn(bookResponseDto);

            ResponseEntity<BookResponseDto> response = bookController.getByIsbn("1234567890123");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(bookResponseDto);
            verify(bookService).getByIsbn("1234567890123");
        }

        @Test
        @DisplayName("should throw exception when book with ISBN is not found")
        void shouldThrowExceptionWhenBookWithIsbnIsNotFound() {
            when(bookService.getByIsbn("9999999999999"))
                    .thenThrow(new ResourceNotFoundException("Book", "isbn", "9999999999999"));

            assertThrows(ResourceNotFoundException.class, () -> bookController.getByIsbn("9999999999999"));
            verify(bookService).getByIsbn("9999999999999");
        }
    }

    @Nested
    @DisplayName("GET /api/books/count/author/{authorId}")
    class CountBooksByAuthorTests {

        @Test
        @DisplayName("should return 200 OK with book count for author")
        void shouldReturn200OkWithBookCountForAuthor() {
            when(bookService.countBooksByAuthor(anyLong())).thenReturn(5L);

            ResponseEntity<Long> response = bookController.countBooksByAuthor(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(5L);
            verify(bookService).countBooksByAuthor(1L);
        }

        @Test
        @DisplayName("should throw exception when author is not found")
        void shouldThrowExceptionWhenAuthorIsNotFound() {
            when(bookService.countBooksByAuthor(999L))
                    .thenThrow(new ResourceNotFoundException("Author", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> bookController.countBooksByAuthor(999L));
            verify(bookService).countBooksByAuthor(999L);
        }
    }
}