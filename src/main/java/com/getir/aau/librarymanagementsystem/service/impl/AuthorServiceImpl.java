package com.getir.aau.librarymanagementsystem.service.impl;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.AuthorRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.AuthorResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Author;
import com.getir.aau.librarymanagementsystem.model.mapper.AuthorMapper;
import com.getir.aau.librarymanagementsystem.repository.AuthorRepository;
import com.getir.aau.librarymanagementsystem.service.AuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    @Override
    public AuthorResponseDto create(AuthorRequestDto dto) {
        if (authorRepository.existsByNameIgnoreCase(dto.name())) {
            throw new ResourceAlreadyExistsException("Author", "name", dto.name());
        }
        log.info("Creating new author with name: {}", dto.name());
        Author author = authorMapper.toEntity(dto);
        Author saved = authorRepository.save(author);
        log.info("Author created with ID: {}", saved.getId());
        return authorMapper.toDto(saved);
    }

    @Override
    public AuthorResponseDto update(Long id, AuthorRequestDto dto) {
        log.info("Updating author with ID: {}", id);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", id));

        Author updated = Author.builder()
                .id(author.getId())
                .name(dto.name())
                .description(dto.description())
                .books(author.getBooks())
                .build();

        Author saved = authorRepository.save(updated);
        log.info("Author updated successfully with ID: {}", saved.getId());
        return authorMapper.toDto(saved);
    }

    @Override
    public AuthorResponseDto getById(Long id) {
        log.info("Fetching author by ID: {}", id);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", id));

        return authorMapper.toDto(author);
    }

    @Override
    public List<AuthorResponseDto> getAll() {
        log.info("Fetching all authors");
        return authorRepository.findAll().stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        log.warn("Deleting author with ID: {}", id);

        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author", "id", id);
        }

        authorRepository.deleteById(id);
        log.info("Author deleted successfully with ID: {}", id);
    }
}