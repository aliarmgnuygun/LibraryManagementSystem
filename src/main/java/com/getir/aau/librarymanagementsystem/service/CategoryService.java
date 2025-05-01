package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.dto.CategoryRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.CategoryResponseDto;

import java.util.List;

public interface CategoryService {

    CategoryResponseDto createCategory(CategoryRequestDto dto);
    CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto);
    CategoryResponseDto getCategoryById(Long id);
    List<CategoryResponseDto> getAllCategories();
    void deleteCategory(Long id);

}
