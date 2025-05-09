package com.getir.aau.librarymanagementsystem.model.mapper;

import com.getir.aau.librarymanagementsystem.model.dto.request.CategoryRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.CategoryResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponseDto toDto(Category category);

    Category toEntity(CategoryRequestDto dto);
}