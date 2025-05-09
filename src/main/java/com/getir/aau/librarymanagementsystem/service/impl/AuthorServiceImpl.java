package com.getir.aau.librarymanagementsystem.service.impl;

import com.getir.aau.librarymanagementsystem.model.dto.request.AuthorRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.AuthorResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Author;
import com.getir.aau.librarymanagementsystem.model.mapper.AuthorMapper;
import com.getir.aau.librarymanagementsystem.repository.AuthorRepository;
import com.getir.aau.librarymanagementsystem.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    @Override
    public AuthorResponseDto createAuthor(AuthorRequestDto dto) {
        Author author = authorMapper.toEntity(dto);
        return authorMapper.toDto(authorRepository.save(author));
    }

    @Override
    public AuthorResponseDto updateAuthor(Long id, AuthorRequestDto dto) {
        Author author = authorRepository.findById(id).orElseThrow();

        Author updated = Author.builder()
                .id(author.getId())
                .name(dto.name())
                .description(dto.description())
                .books(author.getBooks())
                .build();

        return authorMapper.toDto(authorRepository.save(updated));
    }


    @Override
    public AuthorResponseDto getAuthorById(Long id) {
        Author author = authorRepository.findById(id).orElseThrow();
        return authorMapper.toDto(author);
    }

    @Override
    public List<AuthorResponseDto> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAuthor(Long id) {
        authorRepository.deleteById(id);
    }
}