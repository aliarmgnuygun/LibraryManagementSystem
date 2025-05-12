package com.getir.aau.librarymanagementsystem.unit.controller;

import com.getir.aau.librarymanagementsystem.controller.AuthorController;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.AuthorRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.AuthorResponseDto;
import com.getir.aau.librarymanagementsystem.service.AuthorService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorController Unit Tests")
class AuthorControllerTest {

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private AuthorController authorController;

    private AuthorRequestDto requestDto;
    private AuthorResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = new AuthorRequestDto("John Doe", "About John Doe");
        responseDto = new AuthorResponseDto(1L, "John Doe", "About John Doe");
    }

    @Nested
    @DisplayName("POST /api/authors")
    class CreateTests {

        @Test
        @DisplayName("should return 201 CREATED when valid input is provided")
        void shouldReturnCreatedStatusWhenValidInputIsProvided() {
            when(authorService.create(any(AuthorRequestDto.class))).thenReturn(responseDto);

            ResponseEntity<AuthorResponseDto> response = authorController.create(requestDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().name()).isEqualTo("John Doe");
            assertThat(response.getBody().description()).isEqualTo("About John Doe");
            verify(authorService, times(1)).create(requestDto);
        }

        @Test
        @DisplayName("should throw exception when input is invalid")
        void shouldThrowExceptionWhenInputIsInvalid() {
            AuthorRequestDto invalidDto = new AuthorRequestDto("", "");

            when(authorService.create(invalidDto)).thenThrow(new IllegalArgumentException("Invalid author data"));

            assertThrows(IllegalArgumentException.class, () ->
                    authorController.create(invalidDto));

            verify(authorService).create(invalidDto);
        }
    }

    @Nested
    @DisplayName("PUT /api/authors/{id}")
    class UpdateTests {

        @Test
        @DisplayName("should return 200 OK when update is successful")
        void shouldReturnOkStatusWhenUpdateIsSuccessful() {
            when(authorService.update(anyLong(), any(AuthorRequestDto.class))).thenReturn(responseDto);

            ResponseEntity<AuthorResponseDto> response = authorController.update(1L, requestDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(1L);
            assertThat(response.getBody().name()).isEqualTo("John Doe");
            verify(authorService).update(1L, requestDto);
        }

        @Test
        @DisplayName("Should throw exception when author id is invalid")
        void shouldThrowExceptionWhenAuthorDoesNotExist() {
            when(authorService.update(eq(99L), any(AuthorRequestDto.class)))
                    .thenThrow(new ResourceNotFoundException("Author", "id", 99L));

            assertThrows(ResourceNotFoundException.class, () ->
                    authorController.update(99L, requestDto));

            verify(authorService).update(eq(99L), any(AuthorRequestDto.class));
        }
    }

    @Nested
    @DisplayName("GET /api/authors/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("should return 200 OK when author is found")
        void shouldReturnOkStatusWhenAuthorIsFound() {
            when(authorService.getById(anyLong())).thenReturn(responseDto);

            ResponseEntity<AuthorResponseDto> response = authorController.getById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(1L);
            verify(authorService).getById(1L);
        }

        @Test
        void shouldThrowNotFoundWhenAuthorDoesNotExist() {
            when(authorService.getById(99L)).thenThrow(new ResourceNotFoundException("Author", "id", 99L));

            assertThrows(ResourceNotFoundException.class, () ->
                    authorController.getById(99L));

            verify(authorService).getById(99L);
        }
    }

    @Nested
    @DisplayName("GET /api/authors")
    class GetAllTests {

        @Test
        @DisplayName("should return 200 OK when authors are found")
        void shouldReturnOkStatusWhenGettingAllAuthors() {
            List<AuthorResponseDto> authors = List.of(responseDto);
            when(authorService.getAll()).thenReturn(authors);

            ResponseEntity<List<AuthorResponseDto>> response = authorController.getAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().getFirst().name()).isEqualTo("John Doe");
            verify(authorService).getAll();
            verify(authorService).getAll();
        }
    }

    @Nested
    @DisplayName("DELETE /api/authors/{id}")
    class DeleteTests {

        @Test
        @DisplayName("should return 200 OK when author is deleted")
        void shouldReturnOkStatusWhenNoAuthorsExist() {
            when(authorService.getAll()).thenReturn(List.of());

            ResponseEntity<List<AuthorResponseDto>> response = authorController.getAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
            verify(authorService).getAll();
        }

        @Test
        @DisplayName("should return 204 NO CONTENT when author is deleted successfully")
        void shouldReturnNoContentStatus() {
            Long authorId = 1L;
            doNothing().when(authorService).delete(authorId);

            ResponseEntity<Void> response = authorController.delete(authorId);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(authorService, times(1)).delete(authorId);
        }

        @Test
        @DisplayName("should handle not found exception when author doesn't exist")
        void shouldHandleNotFoundExceptionWhenAuthorDoesNotExist() {
            Long nonExistentId = 999L;
            doThrow(new EntityNotFoundException("Author not found")).when(authorService).delete(nonExistentId);

            assertThrows(EntityNotFoundException.class, () -> authorController.delete(nonExistentId));
            verify(authorService, times(1)).delete(nonExistentId);
        }

        @Test
        @DisplayName("should handle database exception during deletion")
        void delete_ShouldHandleDatabaseException() {
            Long authorId = 1L;
            doThrow(new DataIntegrityViolationException("Cannot delete author because it is referenced by books"))
                    .when(authorService).delete(authorId);

            assertThrows(DataIntegrityViolationException.class, () -> authorController.delete(authorId));
            verify(authorService, times(1)).delete(authorId);
        }
    }
}