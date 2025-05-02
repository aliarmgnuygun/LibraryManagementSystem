package com.getir.aau.librarymanagementsystem.controller;

import com.getir.aau.librarymanagementsystem.model.dto.BookPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.BookRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.BookResponseDto;
import com.getir.aau.librarymanagementsystem.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "CRUD operations and advanced queries for books")
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Create a new book", responses = {
            @ApiResponse(responseCode = "201", description = "Book created",
                    content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<BookResponseDto> createBook(@Valid @RequestBody BookRequestDto dto) {
        return new ResponseEntity<>(bookService.createBook(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing book", responses = {
            @ApiResponse(responseCode = "200", description = "Book updated",
                    content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> updateBook(@PathVariable Long id,
                                                      @Valid @RequestBody BookRequestDto dto) {
        return ResponseEntity.ok(bookService.updateBook(id, dto));
    }

    @Operation(summary = "Delete a book", responses = {
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get book by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @Operation(summary = "Search books by keyword")
    @GetMapping("/search")
    public ResponseEntity<BookPageResponseDto> searchBooks(@RequestParam(required = false) String keyword,
                                                           Pageable pageable) {
        return ResponseEntity.ok(bookService.searchBooks(keyword != null ? keyword : "", pageable));
    }

    @Operation(summary = "Get books by category ID")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<BookPageResponseDto> getBooksByCategory(@PathVariable Long categoryId, Pageable pageable) {
        return ResponseEntity.ok(bookService.getBooksByCategoryId(categoryId, pageable));
    }

    @Operation(summary = "Get books by author ID")
    @GetMapping("/author/{authorId}")
    public ResponseEntity<BookPageResponseDto> getBooksByAuthor(@PathVariable Long authorId, Pageable pageable) {
        return ResponseEntity.ok(bookService.getBooksByAuthorId(authorId, pageable));
    }

    @Operation(summary = "Get available books")
    @GetMapping("/available")
    public ResponseEntity<BookPageResponseDto> getAvailableBooks(Pageable pageable) {
        return ResponseEntity.ok(bookService.getAvailableBooks(pageable));
    }

    @Operation(summary = "Get books by genre")
    @GetMapping("/genre")
    public ResponseEntity<BookPageResponseDto> getBooksByGenre(@RequestParam String genre, Pageable pageable) {
        return ResponseEntity.ok(bookService.getBooksByGenre(genre, pageable));
    }

    @Operation(summary = "Get books by author name")
    @GetMapping("/author/name")
    public ResponseEntity<BookPageResponseDto> getBooksByAuthorName(@RequestParam String name, Pageable pageable) {
        return ResponseEntity.ok(bookService.getBooksByAuthorName(name, pageable));
    }

    @Operation(summary = "Get books by title")
    @GetMapping("/title")
    public ResponseEntity<BookPageResponseDto> getBooksByTitle(@RequestParam String title, Pageable pageable) {
        return ResponseEntity.ok(bookService.getBooksByTitle(title, pageable));
    }

    @Operation(summary = "Get book by ISBN")
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookResponseDto> getBookByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
    }

    @Operation(summary = "Count books by author ID")
    @GetMapping("/count/author/{authorId}")
    public ResponseEntity<Long> countBooksByAuthor(@PathVariable Long authorId) {
        return ResponseEntity.ok(bookService.countBooksByAuthor(authorId));
    }
}