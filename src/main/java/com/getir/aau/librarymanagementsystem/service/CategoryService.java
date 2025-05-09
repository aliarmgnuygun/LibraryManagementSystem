package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.dto.request.CategoryRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.CategoryResponseDto;

import java.util.List;

public interface CategoryService {

    CategoryResponseDto create(CategoryRequestDto dto);
    CategoryResponseDto update(Long id, CategoryRequestDto dto);
    CategoryResponseDto getById(Long id);
    List<CategoryResponseDto> getAll();
    void delete(Long id);

}