package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.dto.request.AuthorRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.AuthorResponseDto;

import java.util.List;

public interface AuthorService {

    AuthorResponseDto create(AuthorRequestDto dto);
    AuthorResponseDto update(Long id, AuthorRequestDto dto);
    AuthorResponseDto getById(Long id);
    List<AuthorResponseDto> getAll();
    void delete(Long id);
}