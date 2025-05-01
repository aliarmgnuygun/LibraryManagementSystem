package com.getir.aau.librarymanagementsystem.model.mapper;

import com.getir.aau.librarymanagementsystem.model.dto.AuthorRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.AuthorResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    AuthorMapper INSTANCE = Mappers.getMapper(AuthorMapper.class);

    Author toEntity(AuthorRequestDto dto);

    AuthorResponseDto toDto(Author author);
}