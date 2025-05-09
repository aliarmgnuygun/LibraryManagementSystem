package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.dto.request.AuthorRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.AuthorResponseDto;

import java.util.List;

public interface AuthorService {

    AuthorResponseDto createAuthor(AuthorRequestDto dto);
    AuthorResponseDto updateAuthor(Long id, AuthorRequestDto dto);
    AuthorResponseDto getAuthorById(Long id);
    List<AuthorResponseDto> getAllAuthors();
    void deleteAuthor(Long id);

}