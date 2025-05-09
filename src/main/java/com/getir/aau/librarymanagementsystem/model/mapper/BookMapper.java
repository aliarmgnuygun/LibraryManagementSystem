package com.getir.aau.librarymanagementsystem.model.mapper;

import com.getir.aau.librarymanagementsystem.model.dto.request.BookRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookPageResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.BookResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Author;
import com.getir.aau.librarymanagementsystem.model.entity.Book;
import com.getir.aau.librarymanagementsystem.model.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface  BookMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "available", expression = "java(dto.numberOfCopies() > 0)")
    Book toEntity(BookRequestDto dto, Author author, Category category);

    @Mapping(source = "author.name", target = "authorName")
    @Mapping(source = "category.name", target = "categoryName")
    BookResponseDto toDto(Book book);

    @Mapping(target = "books", source = "page.content")
    @Mapping(target = "totalPages", source = "page.totalPages")
    @Mapping(target = "totalItems", source = "page.totalElements")
    BookPageResponseDto toPageDto(Page<BookResponseDto> page);
}