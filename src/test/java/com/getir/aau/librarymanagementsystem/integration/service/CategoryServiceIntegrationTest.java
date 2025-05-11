package com.getir.aau.librarymanagementsystem.integration.service;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.CategoryRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.CategoryResponseDto;
import com.getir.aau.librarymanagementsystem.repository.CategoryRepository;
import com.getir.aau.librarymanagementsystem.service.CategoryService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for CategoryService.
 * Uses H2 in-memory databases with test profile.
 */

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CategoryService Integration Tests")
class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    private CategoryRequestDto requestDto;

    @BeforeEach
    void setup() {
        categoryRepository.deleteAll();
        requestDto = new CategoryRequestDto("Science");
    }

    @Nested
    @DisplayName("Create Method Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create category successfully")
        void shouldCreateCategory() {
            CategoryResponseDto result = categoryService.create(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.name()).isEqualTo("Science");

            var fromDb = categoryRepository.findById(result.id()).orElse(null);
            assertThat(fromDb).isNotNull();
            assertThat(fromDb.getName()).isEqualTo("Science");
        }

        @Test
        @DisplayName("Should throw exception when category name already exists")
        void shouldThrowWhenNameExists() {
            categoryService.create(requestDto);

            assertThrows(ResourceAlreadyExistsException.class, () ->
                categoryService.create(requestDto));
        }
    }

    @Nested
    @DisplayName("Update Method Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update existing category")
        void shouldUpdateCategory() {
            CategoryResponseDto created = categoryService.create(requestDto);
            CategoryRequestDto updateDto = new CategoryRequestDto("Updated");

            CategoryResponseDto updated = categoryService.update(created.id(), updateDto);

            assertThat(updated).isNotNull();
            assertThat(updated.name()).isEqualTo("Updated");

            var fromDb = categoryRepository.findById(created.id()).orElse(null);
            assertThat(fromDb).isNotNull();
            assertThat(fromDb.getName()).isEqualTo("Updated");
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent category")
        void shouldThrowWhenUpdatingInvalidId() {
            assertThrows(ResourceNotFoundException.class, () ->
                categoryService.update(999L, requestDto));
        }
    }

    @Nested
    @DisplayName("GetById Method Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return category when found")
        void shouldReturnCategoryById() {
            CategoryResponseDto created = categoryService.create(requestDto);

            CategoryResponseDto result = categoryService.getById(created.id());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(created.id());
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowWhenNotFound() {
            assertThrows(ResourceNotFoundException.class, () ->
                categoryService.getById(999L));
        }
    }

    @Nested
    @DisplayName("GetAll Method Tests")
    class GetAllTests {

        @Test
        @DisplayName("Should return all categories")
        void shouldReturnAllCategories() {
            categoryService.create(requestDto);
            categoryService.create(new CategoryRequestDto("Art"));

            List<CategoryResponseDto> result = categoryService.getAll();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(CategoryResponseDto::name)
                    .containsExactlyInAnyOrder("Science", "Art");
        }
    }

    @Nested
    @DisplayName("Delete Method Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete existing category")
        void shouldDeleteCategory() {
            CategoryResponseDto created = categoryService.create(requestDto);

            categoryService.delete(created.id());

            assertThat(categoryRepository.existsById(created.id())).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent category")
        void shouldThrowWhenDeletingInvalidId() {
            assertThrows(ResourceNotFoundException.class, () ->
                categoryService.delete(999L));
        }
    }
}