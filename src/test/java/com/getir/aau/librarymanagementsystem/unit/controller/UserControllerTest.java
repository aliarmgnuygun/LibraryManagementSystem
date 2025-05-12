package com.getir.aau.librarymanagementsystem.unit.controller;

import com.getir.aau.librarymanagementsystem.controller.UserController;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.UserUpdateRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.UserResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.ERole;
import com.getir.aau.librarymanagementsystem.security.auth.dto.RegisterRequestDto;
import com.getir.aau.librarymanagementsystem.service.UserService;
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
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private RegisterRequestDto registerRequestDto;
    private UserUpdateRequestDto updateRequestDto;
    private UserResponseDto userResponseDto;
    private static final String USER_ROLE = ERole.ROLE_USER.toString();
    private static final String LIBRARIAN_ROLE = ERole.ROLE_LIBRARIAN.toString();

    @BeforeEach
    void setUp() {
        registerRequestDto = new RegisterRequestDto("John", "Doe", "john.doe@example.com", "Password123!","01234567891");
        updateRequestDto = new UserUpdateRequestDto("Jane", "Doe", "jane.doe@example.com","01234567891");
        userResponseDto = new UserResponseDto(1L, "John", "Doe", "john.doe@example.com","01234567891" ,USER_ROLE);
    }

    @Nested
    @DisplayName("POST /api/users")
    class CreateTests {

        @Test
        @DisplayName("should return 200 OK when valid input is provided")
        void shouldReturnOkStatusWhenValidInputIsProvided() {
            when(userService.create(any(RegisterRequestDto.class))).thenReturn(userResponseDto);

            ResponseEntity<UserResponseDto> response = userController.create(registerRequestDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().email()).isEqualTo("john.doe@example.com");
            verify(userService).create(registerRequestDto);
        }

        @Test
        @DisplayName("should throw exception when email is already in use")
        void shouldThrowExceptionWhenEmailIsAlreadyInUse() {
            when(userService.create(any(RegisterRequestDto.class)))
                    .thenThrow(new DataIntegrityViolationException("Email already in use"));

            assertThrows(DataIntegrityViolationException.class, () -> userController.create(registerRequestDto));
            verify(userService).create(registerRequestDto);
        }

        @Test
        @DisplayName("should throw exception when input is invalid")
        void shouldThrowExceptionWhenInputIsInvalid() {
            RegisterRequestDto invalidDto = new RegisterRequestDto("", "", "", "","");

            when(userService.create(invalidDto))
                    .thenThrow(new IllegalArgumentException("Invalid user data"));

            assertThrows(IllegalArgumentException.class, () -> userController.create(invalidDto));
            verify(userService).create(invalidDto);
        }
    }

    @Nested
    @DisplayName("GET /api/users/me")
    class GetMeTests {

        @Test
        @DisplayName("should return 200 OK with current user details")
        void shouldReturnOkStatusWithCurrentUserDetails() {
            when(authentication.getName()).thenReturn("john.doe@example.com");
            when(userService.getByEmail("john.doe@example.com")).thenReturn(userResponseDto);

            ResponseEntity<UserResponseDto> response = userController.getMe(authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().email()).isEqualTo("john.doe@example.com");
            verify(authentication).getName();
            verify(userService).getByEmail("john.doe@example.com");
        }

        @Test
        @DisplayName("should throw exception when user email not found")
        void shouldThrowExceptionWhenUserEmailNotFound() {
            when(authentication.getName()).thenReturn("unknown@example.com");
            when(userService.getByEmail("unknown@example.com"))
                    .thenThrow(new EntityNotFoundException("User not found"));

            assertThrows(EntityNotFoundException.class, () -> userController.getMe(authentication));
            verify(authentication).getName();
            verify(userService).getByEmail("unknown@example.com");
        }
    }

    @Nested
    @DisplayName("GET /api/users/email")
    class GetByEmailTests {

        @Test
        @DisplayName("should return 200 OK when user is found by email")
        void shouldReturnOkStatusWhenUserIsFoundByEmail() {
            when(userService.getByEmail(anyString())).thenReturn(userResponseDto);

            ResponseEntity<UserResponseDto> response = userController.getByEmail("john.doe@example.com");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().email()).isEqualTo("john.doe@example.com");
            verify(userService).getByEmail("john.doe@example.com");
        }

        @Test
        @DisplayName("should throw exception when user not found by email")
        void shouldThrowExceptionWhenUserNotFoundByEmail() {
            when(userService.getByEmail("unknown@example.com"))
                    .thenThrow(new ResourceNotFoundException("User", "email", "unknown@example.com"));

            assertThrows(ResourceNotFoundException.class, () -> userController.getByEmail("unknown@example.com"));
            verify(userService).getByEmail("unknown@example.com");
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("should return 200 OK when user is found by id")
        void shouldReturnOkStatusWhenUserIsFoundById() {
            when(userService.getById(anyLong())).thenReturn(userResponseDto);

            ResponseEntity<UserResponseDto> response = userController.getById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(1L);
            verify(userService).getById(1L);
        }

        @Test
        @DisplayName("should throw exception when user not found by id")
        void shouldThrowExceptionWhenUserNotFoundById() {
            when(userService.getById(999L))
                    .thenThrow(new ResourceNotFoundException("User", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> userController.getById(999L));
            verify(userService).getById(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/users")
    class GetAllTests {

        @Test
        @DisplayName("should return 200 OK with list of users")
        void shouldReturnOkStatusWithListOfUsers() {
            List<UserResponseDto> users = List.of(userResponseDto);
            when(userService.getAll()).thenReturn(users);

            ResponseEntity<List<UserResponseDto>> response = userController.getAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().getFirst().email()).isEqualTo("john.doe@example.com");
            verify(userService).getAll();
        }

        @Test
        @DisplayName("should return 200 OK with empty list when no users exist")
        void shouldReturnOkStatusWithEmptyListWhenNoUsersExist() {
            when(userService.getAll()).thenReturn(Collections.emptyList());

            ResponseEntity<List<UserResponseDto>> response = userController.getAll();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
            verify(userService).getAll();
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateTests {

        @Test
        @DisplayName("should return 200 OK when update is successful")
        void shouldReturnOkStatusWhenUpdateIsSuccessful() {
            UserResponseDto updatedUser = new UserResponseDto(1L, "Jane", "Doe", "jane.doe@example.com","01234567891", USER_ROLE);
            when(userService.update(anyLong(), any(UserUpdateRequestDto.class))).thenReturn(updatedUser);

            ResponseEntity<UserResponseDto> response = userController.update(1L, updateRequestDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().firstName()).isEqualTo("Jane");
            assertThat(response.getBody().email()).isEqualTo("jane.doe@example.com");
            verify(userService).update(1L, updateRequestDto);
        }

        @Test
        @DisplayName("should throw exception when user does not exist")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            when(userService.update(eq(999L), any(UserUpdateRequestDto.class)))
                    .thenThrow(new ResourceNotFoundException("User", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> userController.update(999L, updateRequestDto));
            verify(userService).update(eq(999L), any(UserUpdateRequestDto.class));
        }

        @Test
        @DisplayName("should throw exception when email is already in use")
        void shouldThrowExceptionWhenEmailIsAlreadyInUse() {
            when(userService.update(anyLong(), any(UserUpdateRequestDto.class)))
                    .thenThrow(new DataIntegrityViolationException("Email already in use"));

            assertThrows(DataIntegrityViolationException.class, () -> userController.update(1L, updateRequestDto));
            verify(userService).update(1L, updateRequestDto);
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}/role")
    class ChangeRoleTests {

        @Test
        @DisplayName("should return 200 OK when role change is successful")
        void shouldReturnOkStatusWhenRoleChangeIsSuccessful() {
            UserResponseDto updatedUser = new UserResponseDto(1L, "John", "Doe", "john.doe@example.com", "01234567891", LIBRARIAN_ROLE);
            when(userService.changeRole(anyLong(), any(ERole.class))).thenReturn(updatedUser);

            ResponseEntity<UserResponseDto> response = userController.changeRole(1L, ERole.ROLE_LIBRARIAN);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().role()).isEqualTo(LIBRARIAN_ROLE);
            verify(userService).changeRole(1L, ERole.ROLE_LIBRARIAN);
        }

        @Test
        @DisplayName("should throw exception when user does not exist")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            when(userService.changeRole(eq(999L), any(ERole.class)))
                    .thenThrow(new ResourceNotFoundException("User", "id", 999L));

            assertThrows(ResourceNotFoundException.class, () -> userController.changeRole(999L, ERole.ROLE_LIBRARIAN));
            verify(userService).changeRole(eq(999L), any(ERole.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteTests {

        @Test
        @DisplayName("should return 204 NO CONTENT when user is deleted successfully")
        void shouldReturnNoContentStatusWhenUserIsDeletedSuccessfully() {
            doNothing().when(userService).delete(anyLong());

            ResponseEntity<Void> response = userController.delete(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(userService).delete(1L);
        }

        @Test
        @DisplayName("should throw exception when user does not exist")
        void shouldThrowExceptionWhenUserDoesNotExist() {
            doThrow(new ResourceNotFoundException("User", "id", 999L))
                    .when(userService).delete(999L);

            assertThrows(ResourceNotFoundException.class, () -> userController.delete(999L));
            verify(userService).delete(999L);
        }
    }
}