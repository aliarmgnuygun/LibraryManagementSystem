package com.getir.aau.librarymanagementsystem.controller;

import com.getir.aau.librarymanagementsystem.exception.ExceptionResult;
import com.getir.aau.librarymanagementsystem.model.dto.request.CategoryRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.CategoryResponseDto;
import com.getir.aau.librarymanagementsystem.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "CRUD operations for book categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create a new category", responses = {
            @ApiResponse(responseCode = "201", description = "Category created",
                    content = @Content(schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class))),
            @ApiResponse(responseCode = "409", description = "Category already exists",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
    })
    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(@Valid @RequestBody CategoryRequestDto dto) {
        return new ResponseEntity<>(categoryService.create(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing category", responses = {
            @ApiResponse(responseCode = "200", description = "Category updated",
                    content = @Content(schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> update(@PathVariable Long id,
                                                              @Valid @RequestBody CategoryRequestDto dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @Operation(summary = "Get category by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Category found",
                    content = @Content(schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @Operation(summary = "Get all categories", responses = {
            @ApiResponse(responseCode = "200", description = "List of categories retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponseDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @Operation(summary = "Delete a category", responses = {
            @ApiResponse(responseCode = "204", description = "Category deleted"),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}