package com.getir.aau.librarymanagementsystem.model.mapper;

import com.getir.aau.librarymanagementsystem.model.dto.request.AuthorRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.AuthorResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Author;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    Author toEntity(AuthorRequestDto dto);

    AuthorResponseDto toDto(Author author);
}