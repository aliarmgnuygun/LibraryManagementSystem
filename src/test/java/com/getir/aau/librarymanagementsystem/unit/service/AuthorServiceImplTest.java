package com.getir.aau.librarymanagementsystem.unit.service;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.AuthorRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.AuthorResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Author;
import com.getir.aau.librarymanagementsystem.model.mapper.AuthorMapper;
import com.getir.aau.librarymanagementsystem.repository.AuthorRepository;
import com.getir.aau.librarymanagementsystem.service.impl.AuthorServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuthorMapper authorMapper;

    @InjectMocks
    private AuthorServiceImpl authorService;

    private static Author author;
    private static AuthorRequestDto requestDto;
    private static AuthorResponseDto responseDto;

    @BeforeAll
    static void setup() {
        author = Author.builder()
                .id(1L)
                .name("Test Author")
                .description("Test Description")
                .build();

        requestDto = new AuthorRequestDto("Test Author", "Test Description");
        responseDto = new AuthorResponseDto(1L, "Test Author", "Test Description");
    }

    @Nested
    @DisplayName("Create Method Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create and return author when name doesn't exist")
        void createAuthorWhenNameDoesNotExist() {
            given(authorRepository.existsByNameIgnoreCase(anyString())).willReturn(false);
            given(authorMapper.toEntity(any())).willReturn(author);
            given(authorRepository.save(any())).willReturn(author);
            given(authorMapper.toDto(any())).willReturn(responseDto);

            AuthorResponseDto result = authorService.create(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Test Author");
        }

        @Test
        @DisplayName("Should throw exception when name already exists")
        void createAuthorWhenNameAlreadyExists() {
            given(authorRepository.existsByNameIgnoreCase(anyString())).willReturn(true);

            assertThrows(ResourceAlreadyExistsException.class,
                    () -> authorService.create(requestDto));
        }
    }

    @Nested
    @DisplayName("Update Method Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update and return author when found")
        void updateAuthorWhenFound() {
            Author updatedAuthor = Author.builder()
                    .id(1L)
                    .name("Updated Author")
                    .description("Updated Description")
                    .build();

            AuthorRequestDto updateRequestDto = new AuthorRequestDto("Updated Author", "Updated Description");
            AuthorResponseDto updatedResponseDto = new AuthorResponseDto(1L, "Updated Author", "Updated Description");

            given(authorRepository.findById(anyLong())).willReturn(Optional.of(author));
            given(authorRepository.save(any())).willReturn(updatedAuthor);
            given(authorMapper.toDto(any())).willReturn(updatedResponseDto);

            AuthorResponseDto result = authorService.update(1L, updateRequestDto);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Updated Author");
        }

        @Test
        @DisplayName("Should throw exception when author not found")
        void updateAuthorWhenNotFound() {
            given(authorRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> authorService.update(1L, requestDto));
        }
    }

    @Nested
    @DisplayName("GetById Method Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return author when found")
        void getByIdWhenAuthorFound() {
            given(authorRepository.findById(anyLong())).willReturn(Optional.of(author));
            given(authorMapper.toDto(any())).willReturn(responseDto);

            AuthorResponseDto result = authorService.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Test Author");
        }

        @Test
        @DisplayName("Should throw exception when author not found")
        void getByIdWhenAuthorNotFound() {
            given(authorRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> authorService.getById(1L));
        }
    }

    @Nested
    @DisplayName("GetAll Method Tests")
    class GetAllTests {

        @Test
        @DisplayName("Should return all authors")
        void getAllAuthors() {
            Author author2 = Author.builder()
                    .id(2L)
                    .name("Test Author 2")
                    .description("Test Description 2")
                    .build();

            AuthorResponseDto responseDto2 = new AuthorResponseDto(2L, "Test Author 2", "Test Description 2");

            given(authorRepository.findAll()).willReturn(Arrays.asList(author, author2));
            given(authorMapper.toDto(author)).willReturn(responseDto);
            given(authorMapper.toDto(author2)).willReturn(responseDto2);

            List<AuthorResponseDto> result = authorService.getAll();

            assertThat(result).isNotNull();
            assertThat(result.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Delete Method Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete author when found")
        void deleteAuthorWhenFound() {
            given(authorRepository.existsById(anyLong())).willReturn(true);
            doNothing().when(authorRepository).deleteById(anyLong());

            authorService.delete(1L);

            verify(authorRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw exception when author not found")
        void deleteAuthorWhenNotFound() {
            given(authorRepository.existsById(anyLong())).willReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> authorService.delete(1L));
        }
    }
}