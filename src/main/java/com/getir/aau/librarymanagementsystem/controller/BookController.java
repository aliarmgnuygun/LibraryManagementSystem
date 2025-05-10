package com.getir.aau.librarymanagementsystem.controller;

import com.getir.aau.librarymanagementsystem.exception.ExceptionResult;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.request.BookRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookResponseDto;
import com.getir.aau.librarymanagementsystem.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "CRUD operations and advanced queries for books")
public class BookController {

    private final BookService bookService;

    @Operation(
            summary = "Create a new book",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Book created",
                            content = @Content(schema = @Schema(implementation = BookResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Book with this ISBN already exists",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    )
            }
    )
    @PostMapping
    public ResponseEntity<BookResponseDto> create(@Valid @RequestBody BookRequestDto dto) {
        return new ResponseEntity<>(bookService.create(dto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update an existing book",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Book updated",
                            content = @Content(schema = @Schema(implementation = BookResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Book not found",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "ISBN conflict with another book",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    )
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> update(@PathVariable Long id,
                                                  @Valid @RequestBody BookRequestDto dto) {
        return ResponseEntity.ok(bookService.update(id, dto));
    }


    @Operation(
            summary = "Delete a book",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Book deleted successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Book not found",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get book by ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Book found",
                            content = @Content(schema = @Schema(implementation = BookResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "304",
                            description = "Not modified (when using ETag)"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Book not found",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    @Operation(
            summary = "Search books by keyword (title, authorName, genre, isbn, categoryName)",
            description = "Search across multiple fields with pagination support",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Page number (zero-based)",
                            example = "0",
                            schema = @Schema(type = "integer", defaultValue = "0")
                    ),
                    @Parameter(
                            name = "size",
                            description = "Page size",
                            example = "10",
                            schema = @Schema(type = "integer", defaultValue = "10")
                    ),
                    @Parameter(
                            name = "sort",
                            description = "Sort field and direction (e.g. id,asc)",
                            example = "id,asc",
                            schema = @Schema(type = "string")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Books found",
                            content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    )
            }
    )
    @GetMapping("/search")
    public ResponseEntity<BookPageResponseDto> searchByKeywords(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(direction = Sort.Direction.ASC) @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(bookService.searchByKeywords(keyword, pageable));
    }

    @Operation(
            summary = "Get books by category ID",
            parameters = {
                    @Parameter(name = "page", description = "Page number (zero-based)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "10"),
                    @Parameter(name = "sort", description = "Sort field and direction", example = "id,asc")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Books retrieved successfully",
                            content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Category not found",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    )
            }
    )
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<BookPageResponseDto> getByCategoryId(@PathVariable Long categoryId,@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(bookService.getByCategoryId(categoryId, pageable));
    }

    @Operation(
            summary = "Get books by author ID",
            parameters = {
                    @Parameter(name = "page", description = "Page number (zero-based)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Page size", example = "10", schema = @Schema(type = "integer", defaultValue = "10")),
                    @Parameter(name = "sort", description = "Sort field and direction (e.g. id,asc)", example = "id,asc", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Books retrieved successfully",
                            content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Author not found",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    )
            }
    )
    @GetMapping("/author/{authorId}")
    public ResponseEntity<BookPageResponseDto> getByAuthorId(@PathVariable Long authorId,@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(bookService.getByAuthorId(authorId, pageable));
    }

    @Operation(
            summary = "Get available books",
            parameters = {
                    @Parameter(name = "page", description = "Page number (zero-based)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Page size", example = "10", schema = @Schema(type = "integer", defaultValue = "10")),
                    @Parameter(name = "sort", description = "Sort field and direction (e.g. id,asc)", example = "id,asc", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Available books retrieved",
                            content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))
                    )
            }
    )
    @GetMapping("/available")
    public ResponseEntity<BookPageResponseDto> getAvailable(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(bookService.getAvailable(pageable));
    }

    @Operation(
            summary = "Get unavailable books",
            parameters = {
                    @Parameter(name = "page", description = "Page number (zero-based)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Page size", example = "10", schema = @Schema(type = "integer", defaultValue = "10")),
                    @Parameter(name = "sort", description = "Sort field and direction (e.g. id,asc)", example = "id,asc", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Unavailable books retrieved",
                            content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))
                    )
            }
    )
    @GetMapping("/unavailable")
    public ResponseEntity<BookPageResponseDto> getUnavailable(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(bookService.getUnavailable(pageable));
    }

    @Operation(
            summary = "Get books by genre",
            parameters = {
                    @Parameter(name = "genre", description = "Book genre to filter by", required = true, example = "Fantasy"),
                    @Parameter(name = "page", description = "Page number (zero-based)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Page size", example = "10", schema = @Schema(type = "integer", defaultValue = "10")),
                    @Parameter(name = "sort", description = "Sort field and direction (e.g. id,asc)", example = "id,asc", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Books retrieved by genre",
                            content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid genre parameter",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    )
            }
    )
    @GetMapping("/genre")
    public ResponseEntity<BookPageResponseDto> getByGenre(@RequestParam String genre, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(bookService.getByGenre(genre, pageable));
    }

    @Operation(
            summary = "Get books by author name",
            parameters = {
                    @Parameter(name = "name", description = "Author name to search for", required = true, example = "J.K. Rowling"),
                    @Parameter(name = "page", description = "Page number (zero-based)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Page size", example = "20", schema = @Schema(type = "integer", defaultValue = "20")),
                    @Parameter(name = "sort", description = "Sort field and direction (e.g. id,asc)", example = "id,asc", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Books retrieved by author name",
                            content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid author name",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    )
            }
    )
    @GetMapping("/author/name")
    public ResponseEntity<BookPageResponseDto> getByAuthorName(@RequestParam String name,@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(bookService.getByAuthorName(name, pageable));
    }

    @Operation(
            summary = "Get books by title",
            parameters = {
                    @Parameter(name = "title", description = "Book title to search for", required = true, example = "Harry Potter"),
                    @Parameter(name = "page", description = "Page number (zero-based)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Page size", example = "20", schema = @Schema(type = "integer", defaultValue = "20")),
                    @Parameter(name = "sort", description = "Sort field and direction (e.g. publicationDate,desc)", example = "publicationDate,desc", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Books retrieved by title",
                            content = @Content(schema = @Schema(implementation = BookPageResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid title parameter",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class))
                    )
            }
    )
    @GetMapping("/title")
    public ResponseEntity<BookPageResponseDto> getByTitle(@RequestParam String title,@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(bookService.getByTitle(title, pageable));
    }


    @Operation(summary = "Get book by ISBN", responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book retrieved by ISBN",
                    content = @Content(schema = @Schema(implementation = BookResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book with specified ISBN not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class))
            )
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