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
    public ResponseEntity<BookResponseDto> create(@Valid @RequestBody BookRequestDto dto) {
        return new ResponseEntity<>(bookService.create(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing book", responses = {
            @ApiResponse(responseCode = "200", description = "Book updated",
                    content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> update(@PathVariable Long id,
                                                  @Valid @RequestBody BookRequestDto dto) {
        return ResponseEntity.ok(bookService.update(id, dto));
    }

    @Operation(summary = "Delete a book", responses = {
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get book by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    @Operation(summary = "Search books by keyword (title, authorName, genre, isbn, categoryName)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Books found",
                            content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            })
    @GetMapping("/search")
    public ResponseEntity<BookPageResponseDto> searchByKeywords(@RequestParam(required = false) String keyword,
                                                                Pageable pageable) {
        return ResponseEntity.ok(bookService.searchByKeywords(keyword != null ? keyword : "", pageable));
    }

    @Operation(summary = "Get books by category ID", responses = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<BookPageResponseDto> getByCategoryId(@PathVariable Long categoryId, Pageable pageable) {
        return ResponseEntity.ok(bookService.getByCategoryId(categoryId, pageable));
    }

    @Operation(summary = "Get books by author ID", responses = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/author/{authorId}")
    public ResponseEntity<BookPageResponseDto> getByAuthorId(@PathVariable Long authorId, Pageable pageable) {
        return ResponseEntity.ok(bookService.getByAuthorId(authorId, pageable));
    }

    @Operation(summary = "Get available books", responses = {
            @ApiResponse(responseCode = "200", description = "Available books retrieved",
                    content = @Content(schema = @Schema(implementation = BookPageResponseDto.class)))
    })
    @GetMapping("/available")
    public ResponseEntity<BookPageResponseDto> getAvailable(Pageable pageable) {
        return ResponseEntity.ok(bookService.getAvailable(pageable));
    }

    @Operation(summary = "Get unavailable books", responses = {
            @ApiResponse(responseCode = "200", description = "Unavailable books retrieved",
                    content = @Content(schema = @Schema(implementation = BookPageResponseDto.class)))
    })
    @GetMapping("/unavailable")
    public ResponseEntity<BookPageResponseDto> getUnavailable(Pageable pageable) {
        return ResponseEntity.ok(bookService.getUnavailable(pageable));
    }

    @Operation(summary = "Get books by genre", responses = {
            @ApiResponse(responseCode = "200", description = "Books retrieved by genre",
                    content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid genre parameter")
    })
    @GetMapping("/genre")
    public ResponseEntity<BookPageResponseDto> getByGenre(@RequestParam String genre, Pageable pageable) {
        return ResponseEntity.ok(bookService.getByGenre(genre, pageable));
    }

    @Operation(summary = "Get books by author name", responses = {
            @ApiResponse(responseCode = "200", description = "Books retrieved by author name",
                    content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid author name")
    })
    @GetMapping("/author/name")
    public ResponseEntity<BookPageResponseDto> getByAuthorName(@RequestParam String name, Pageable pageable) {
        return ResponseEntity.ok(bookService.getByAuthorName(name, pageable));
    }

    @Operation(summary = "Get books by title", responses = {
            @ApiResponse(responseCode = "200", description = "Books retrieved by title",
                    content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid title parameter")
    })
    @GetMapping("/title")
    public ResponseEntity<BookPageResponseDto> getByTitle(@RequestParam String title, Pageable pageable) {
        return ResponseEntity.ok(bookService.getByTitle(title, pageable));
    }

    @Operation(summary = "Get book by ISBN", responses = {
            @ApiResponse(responseCode = "200", description = "Book retrieved by ISBN",
                    content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Book with specified ISBN not found")
    })
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookResponseDto> getByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.getByIsbn(isbn));
    }

    @Operation(summary = "Count books by author ID", responses = {
            @ApiResponse(responseCode = "200", description = "Book count retrieved"),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/count/author/{authorId}")
    public ResponseEntity<Long> countBooksByAuthor(@PathVariable Long authorId) {
        return ResponseEntity.ok(bookService.countBooksByAuthor(authorId));
    }
}