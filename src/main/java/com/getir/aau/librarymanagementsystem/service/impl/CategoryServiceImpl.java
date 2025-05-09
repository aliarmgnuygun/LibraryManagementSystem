package com.getir.aau.librarymanagementsystem.service.impl;

import com.getir.aau.librarymanagementsystem.model.dto.request.CategoryRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.CategoryResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Category;
import com.getir.aau.librarymanagementsystem.model.mapper.CategoryMapper;
import com.getir.aau.librarymanagementsystem.repository.CategoryRepository;
import com.getir.aau.librarymanagementsystem.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        Category category = categoryMapper.toEntity(dto);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto) {
        Category existing = categoryRepository.findById(id).orElseThrow();

        Category updated = Category.builder()
                .id(existing.getId())
                .name(dto.name())
                .books(existing.getBooks())
                .build();

        return categoryMapper.toDto(categoryRepository.save(updated));
    }

    @Override
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow();
        return categoryMapper.toDto(category);
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}