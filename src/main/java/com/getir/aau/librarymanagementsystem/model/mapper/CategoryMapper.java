package com.getir.aau.librarymanagementsystem.model.mapper;

import com.getir.aau.librarymanagementsystem.model.dto.CategoryRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.CategoryResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    CategoryResponseDto toDto(Category category);

    Category toEntity(CategoryRequestDto dto);
}