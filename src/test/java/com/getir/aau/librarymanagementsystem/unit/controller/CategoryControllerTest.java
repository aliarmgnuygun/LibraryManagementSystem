package com.getir.aau.librarymanagementsystem.unit.controller;

import com.getir.aau.librarymanagementsystem.controller.CategoryController;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.CategoryRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.CategoryResponseDto;
import com.getir.aau.librarymanagementsystem.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController Unit Tests")
public class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryRequestDto requestDto;
    private CategoryResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new CategoryRequestDto("Fiction");
        responseDto = new CategoryResponseDto(1L, "Fiction");
    }

    @Nested
    @DisplayName("POST /api/categories")
    class CreateTests {

        @Test
        @DisplayName("should return 201 CREATED when valid input is provided")
        void shouldReturnCreatedStatusWhenValidInputIsProvided() {
            when(categoryService.create(any(CategoryRequestDto.class))).thenReturn(responseDto);

            ResponseEntity<CategoryResponseDto> response = categoryController.create(requestDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().name()).isEqualTo("Fiction");
            verify(categoryService, times(1)).create(requestDto);
        }

        @Test
        @DisplayName("should throw exception when input is invalid")
        void shouldThrowExceptionWhenInputIsInvalid() {
            CategoryRequestDto invalidDto = new CategoryRequestDto("");

            when(categoryService.create(invalidDto)).thenThrow(new IllegalArgumentException("Invalid category data"));

            assertThrows(IllegalArgumentException.class, () -> categoryController.create(invalidDto));
            verify(categoryService).create(invalidDto);
        }

        @Test
        @DisplayName("should throw exception when category already exists")
        void shouldThrowExceptionWhenCategoryAlreadyExists() {
            when(categoryService.create(requestDto)).thenThrow(new DataIntegrityViolationException("Category already exists"));

            assertThrows(DataIntegrityViolationException.class, () -> categoryController.create(requestDto));
            verify(categoryService).create(requestDto);
        }
    }

    @Nested
    @DisplayName("PUT /api/categories/{id}")
    class UpdateTests {

        @Test
        @DisplayName("should return 200 OK when update is successful")
        void shouldReturnOkStatusWhenUpdateIsSuccessful() {
            when(categoryService.update(anyLong(), any(CategoryRequestDto.class))).thenReturn(responseDto);

            ResponseEntity<CategoryResponseDto> response = categoryController.update(1L, requestDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(1L);
            assertThat(response.getBody().name()).isEqualTo("Fiction");
            verify(categoryService).update(1L, requestDto);
        }

        @Test
        @DisplayName("should throw exception when category does not exist")
        void shouldThrowExceptionWhenCategoryDoesNotExist() {
            when(categoryService.update(eq(99L), any(CategoryRequestDto.class)))
                    .thenThrow(new ResourceNotFoundException("Category", "id", 99L));

            assertThrows(ResourceNotFoundException.class, () -> categoryController.update(99L, requestDto));
            verify(categoryService).update(eq(99L), any(CategoryRequestDto.class));
        }
    }

    @Nested
    @DisplayName("GET /api/categories/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("should return 200 OK when category is found")
        void shouldReturnOkStatusWhenCategoryIsFound() {
            when(categoryService.getById(anyLong())).thenReturn(responseDto);

            ResponseEntity<CategoryResponseDto> response = categoryController.getById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(1L);
            verify(categoryService).getById(1L);
        }

        @Test
        @DisplayName("should throw exception when category does not exist")
        void shouldThrowExceptionWhenCategoryDoesNotExist() {
            when(categoryService.getById(99L)).thenThrow(new ResourceNotFoundException("Category", "id", 99L));

            assertThrows(ResourceNotFoundException.class, () -> categoryController.getById(99L));
            verify(categoryService).getById(99L);
        }
    }

    @Nested
    @DisplayName("GET /api/categories")
    class GetAllTests {

        @Test
        @DisplayName("should return 200 OK with list of categories")
        void shouldReturnOkStatusWithListOfCategories() {
            List<CategoryResponseDto> categories = List.of(responseDto);
            when(categoryService.getAll()).thenReturn(categories);

            ResponseEntity<List<CategoryResponseDto>> response = categoryController.getAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().getFirst().name()).isEqualTo("Fiction");
            verify(categoryService).getAll();
        }

        @Test
        @DisplayName("should return 200 OK with empty list when no categories exist")
        void shouldReturnOkStatusWithEmptyListWhenNoCategoriesExist() {
            when(categoryService.getAll()).thenReturn(Collections.emptyList());

            ResponseEntity<List<CategoryResponseDto>> response = categoryController.getAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
            verify(categoryService).getAll();
        }
    }

    @Nested
    @DisplayName("DELETE /api/categories/{id}")
    class DeleteTests {

        @Test
        @DisplayName("should return 204 NO CONTENT when category is deleted successfully")
        void shouldReturnNoContentStatusWhenCategoryIsDeletedSuccessfully() {
            Long categoryId = 1L;
            doNothing().when(categoryService).delete(categoryId);

            ResponseEntity<Void> response = categoryController.delete(categoryId);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(categoryService, times(1)).delete(categoryId);
        }

        @Test
        @DisplayName("should throw exception when category does not exist")
        void shouldThrowExceptionWhenCategoryDoesNotExist() {
            Long nonExistentId = 999L;
            doThrow(new EntityNotFoundException("Category not found")).when(categoryService).delete(nonExistentId);

            assertThrows(EntityNotFoundException.class, () -> categoryController.delete(nonExistentId));
            verify(categoryService, times(1)).delete(nonExistentId);
        }

        @Test
        @DisplayName("should throw exception when category cannot be deleted due to references")
        void shouldThrowExceptionWhenCategoryHasReferences() {
            Long categoryId = 1L;
            doThrow(new DataIntegrityViolationException("Cannot delete category because it is referenced by books"))
                    .when(categoryService).delete(categoryId);

            assertThrows(DataIntegrityViolationException.class, () -> categoryController.delete(categoryId));
            verify(categoryService, times(1)).delete(categoryId);
        }
    }
}