package com.getir.aau.librarymanagementsystem.unit.service;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.CategoryRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.CategoryResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Category;
import com.getir.aau.librarymanagementsystem.model.mapper.CategoryMapper;
import com.getir.aau.librarymanagementsystem.repository.CategoryRepository;
import com.getir.aau.librarymanagementsystem.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryRequestDto requestDto;
    private CategoryResponseDto responseDto;

    @BeforeEach
    void setup() {
        category = Category.builder()
                .id(1L)
                .name("Science")
                .build();

        requestDto = new CategoryRequestDto("Science");
        responseDto = new CategoryResponseDto(1L, "Science");
    }

    @Nested
    @DisplayName("Create Method Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create category when name does not exist")
        void shouldCreateCategory() {
            given(categoryRepository.existsByNameIgnoreCase(anyString())).willReturn(false);
            given(categoryMapper.toEntity(any())).willReturn(category);
            given(categoryRepository.save(any())).willReturn(category);
            given(categoryMapper.toDto(any())).willReturn(responseDto);

            CategoryResponseDto result = categoryService.create(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("Science");
        }

        @Test
        @DisplayName("Should throw exception when category name already exists")
        void shouldThrowWhenCategoryExists() {
            given(categoryRepository.existsByNameIgnoreCase(anyString())).willReturn(true);

            assertThrows(ResourceAlreadyExistsException.class, () -> categoryService.create(requestDto));
        }
    }

    @Nested
    @DisplayName("Update Method Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update existing category")
        void shouldUpdateCategory() {
            Category updatedCategory = Category.builder().id(1L).name("History").build();
            CategoryRequestDto updateDto = new CategoryRequestDto("History");
            CategoryResponseDto updatedResponse = new CategoryResponseDto(1L, "History");

            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(category));
            given(categoryRepository.save(any())).willReturn(updatedCategory);
            given(categoryMapper.toDto(any())).willReturn(updatedResponse);

            CategoryResponseDto result = categoryService.update(1L, updateDto);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("History");
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent category")
        void shouldThrowWhenCategoryNotFound() {
            given(categoryRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> categoryService.update(99L, requestDto));
        }
    }

    @Nested
    @DisplayName("GetById Method Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return category when found")
        void shouldReturnCategoryById() {
            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(category));
            given(categoryMapper.toDto(any())).willReturn(responseDto);

            CategoryResponseDto result = categoryService.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Science");
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowWhenNotFound() {
            given(categoryRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> categoryService.getById(999L));
        }
    }

    @Nested
    @DisplayName("GetAll Method Tests")
    class GetAllTests {

        @Test
        @DisplayName("Should return all categories")
        void shouldReturnAllCategories() {
            Category cat2 = Category.builder().id(2L).name("Art").build();
            CategoryResponseDto res2 = new CategoryResponseDto(2L, "Art");

            given(categoryRepository.findAll()).willReturn(List.of(category, cat2));
            given(categoryMapper.toDto(category)).willReturn(responseDto);
            given(categoryMapper.toDto(cat2)).willReturn(res2);

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
        @DisplayName("Should delete category when found")
        void shouldDeleteCategory() {
            given(categoryRepository.existsById(anyLong())).willReturn(true);
            doNothing().when(categoryRepository).deleteById(anyLong());

            categoryService.delete(1L);

            verify(categoryRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent category")
        void shouldThrowWhenDeletingNonExistentCategory() {
            given(categoryRepository.existsById(anyLong())).willReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(999L));
        }
    }
}