package com.getir.aau.librarymanagementsystem.integration.service;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.AuthorRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.AuthorResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Author;
import com.getir.aau.librarymanagementsystem.repository.AuthorRepository;
import com.getir.aau.librarymanagementsystem.service.AuthorService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for AuthorService.
 * Uses H2 in-memory databases with test profile.
 */

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthorService Integration Tests")
class AuthorServiceIntegrationTest {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    private AuthorRequestDto requestDto;

    @BeforeEach
    void setup() {
        authorRepository.deleteAll();
        requestDto = new AuthorRequestDto("Test Author", "Test Description");
    }

    @Nested
    @DisplayName("Create Method Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create author successfully")
        void shouldCreateAuthor() {
            AuthorResponseDto result = authorService.create(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.name()).isEqualTo("Test Author");

            Author saved = authorRepository.findById(result.id()).orElse(null);
            assertThat(saved).isNotNull();
            assertThat(saved.getName()).isEqualTo("Test Author");
        }

        @Test
        @DisplayName("Should throw exception when creating duplicate author")
        void shouldThrowWhenCreatingDuplicateAuthor() {
            authorService.create(requestDto);

            assertThrows(ResourceAlreadyExistsException.class, () -> authorService.create(requestDto));
        }
    }

    @Nested
    @DisplayName("Update Method Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update author successfully")
        void shouldUpdateAuthor() {
            AuthorResponseDto created = authorService.create(requestDto);
            AuthorRequestDto updateRequest = new AuthorRequestDto("Updated Author", "Updated Description");

            AuthorResponseDto updated = authorService.update(created.id(), updateRequest);

            assertThat(updated).isNotNull();
            assertThat(updated.id()).isEqualTo(created.id());
            assertThat(updated.name()).isEqualTo("Updated Author");

            Author fromDb = authorRepository.findById(updated.id()).orElse(null);
            assertThat(fromDb).isNotNull();
            assertThat(fromDb.getName()).isEqualTo("Updated Author");
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent author")
        void shouldThrowWhenUpdatingNonExistentAuthor() {
            assertThrows(ResourceNotFoundException.class, () ->
                authorService.update(999L, requestDto));
        }
    }

    @Nested
    @DisplayName("GetById Method Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return author when found")
        void shouldReturnAuthorById() {
            AuthorResponseDto created = authorService.create(requestDto);

            AuthorResponseDto found = authorService.getById(created.id());

            assertThat(found).isNotNull();
            assertThat(found.id()).isEqualTo(created.id());
        }

        @Test
        @DisplayName("Should throw exception when author not found")
        void shouldThrowWhenAuthorNotFound() {
            assertThrows(ResourceNotFoundException.class, () ->
                authorService.getById(999L));
        }
    }

    @Nested
    @DisplayName("GetAll Method Tests")
    class GetAllTests {

        @Test
        @DisplayName("Should return all authors")
        void shouldReturnAllAuthors() {
            authorService.create(requestDto);
            authorService.create(new AuthorRequestDto("Another Author", "Another Description"));

            List<AuthorResponseDto> authors = authorService.getAll();

            assertThat(authors).hasSize(2);
            assertThat(authors).extracting(AuthorResponseDto::name)
                    .containsExactlyInAnyOrder("Test Author", "Another Author");
        }
    }

    @Nested
    @DisplayName("Delete Method Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete author successfully")
        void shouldDeleteAuthor() {
            AuthorResponseDto created = authorService.create(requestDto);

            authorService.delete(created.id());

            assertThat(authorRepository.existsById(created.id())).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent author")
        void shouldThrowWhenDeletingNonExistentAuthor() {
            assertThrows(ResourceNotFoundException.class, () ->
                authorService.delete(999L));
        }
    }
}