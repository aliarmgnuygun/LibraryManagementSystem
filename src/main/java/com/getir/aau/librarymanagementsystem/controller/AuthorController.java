package com.getir.aau.librarymanagementsystem.controller;

import com.getir.aau.librarymanagementsystem.model.dto.request.AuthorRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.AuthorResponseDto;
import com.getir.aau.librarymanagementsystem.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Tag(name = "Author Management", description = "CRUD operations for authors")
public class AuthorController {

    private final AuthorService authorService;

    @Operation(summary = "Create a new author", responses = {
            @ApiResponse(responseCode = "201", description = "Author created",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<AuthorResponseDto> create(@Valid @RequestBody AuthorRequestDto dto) {
        return new ResponseEntity<>(authorService.createAuthor(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing author")
    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponseDto> update(@PathVariable Long id,
                                                          @Valid @RequestBody AuthorRequestDto dto) {
        return ResponseEntity.ok(authorService.updateAuthor(id, dto));
    }

    @Operation(summary = "Get author by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.getAuthorById(id));
    }

    @Operation(summary = "Get all authors")
    @GetMapping
    public ResponseEntity<List<AuthorResponseDto>> getAll() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    @Operation(summary = "Delete an author")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}