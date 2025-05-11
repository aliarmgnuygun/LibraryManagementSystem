package com.getir.aau.librarymanagementsystem.unit.service;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.UserUpdateRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.UserResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.ERole;
import com.getir.aau.librarymanagementsystem.model.entity.Role;
import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.model.mapper.UserMapper;
import com.getir.aau.librarymanagementsystem.repository.RoleRepository;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.security.auth.dto.RegisterRequestDto;
import com.getir.aau.librarymanagementsystem.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private Role userRole;
    private Role librarianRole;
    private User user;
    private RegisterRequestDto registerDto;
    private UserResponseDto responseDto;

    @BeforeEach
    void setup() {
        userRole = Role.builder().id(1L).name(ERole.ROLE_USER).build();
        librarianRole = Role.builder().id(2L).name(ERole.ROLE_LIBRARIAN).build();

        user = User.builder()
                .id(1L)
                .firstName("FirstName")
                .lastName("LastName")
                .email("mail@example.com")
                .password("encoded")
                .role(userRole)
                .phoneNumber("05551234567")
                .build();

        registerDto = new RegisterRequestDto("FirstName", "LastName", "mail@example.com", "123456", "05551234567");

        responseDto = new UserResponseDto(1L, "FirstName", "LastName", "mail@example.com", "05551234567", "ROLE_USER");
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUser() {
            given(userRepository.existsByEmail(registerDto.email())).willReturn(false);
            given(roleRepository.findByName(ERole.ROLE_USER)).willReturn(Optional.of(userRole));

            User userToSave = User.builder()
                    .firstName(registerDto.firstName())
                    .lastName(registerDto.lastName())
                    .email(registerDto.email())
                    .password("encoded")
                    .phoneNumber(registerDto.phoneNumber())
                    .role(userRole)
                    .build();

            given(userMapper.fromRegisterRequest(registerDto)).willReturn(userToSave);
            given(userRepository.save(any(User.class))).willReturn(userToSave);
            given(userMapper.toDto(userToSave)).willReturn(responseDto);

            UserResponseDto result = userService.create(registerDto);

            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo(registerDto.email());
            assertThat(result.firstName()).isEqualTo(registerDto.firstName());
            assertThat(result.lastName()).isEqualTo(registerDto.lastName());
            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo("mail@example.com");
        }

        @Test
        @DisplayName("Should throw if email exists")
        void shouldThrowWhenEmailExists() {
            given(userRepository.existsByEmail(anyString())).willReturn(true);

            ResourceAlreadyExistsException exception = assertThrows(
                    ResourceAlreadyExistsException.class,
                    () -> userService.create(registerDto)
            );

            assertThat(exception.getMessage()).contains(registerDto.email());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw if default role not found")
        void shouldThrowWhenRoleMissing() {
            given(userRepository.existsByEmail(registerDto.email())).willReturn(false);
            given(roleRepository.findByName(ERole.ROLE_USER)).willReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.create(registerDto)
            );

            assertThat(exception.getMessage()).contains("ROLE_USER");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should get user by email")
        void shouldGetByEmail() {
            String email = "mail@example.com";
            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(userMapper.toDto(user)).willReturn(responseDto);

            UserResponseDto result = userService.getByEmail(email);

            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo(email);
            assertThat(result.id()).isEqualTo(user.getId());
        }

        @Test
        @DisplayName("Should throw when user not found by email")
        void shouldThrowByEmailNotFound() {
            String nonExistingEmail = "nonexisting@example.com";
            given(userRepository.findByEmail(nonExistingEmail)).willReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.getByEmail(nonExistingEmail)
            );

            assertThat(exception.getMessage()).contains(nonExistingEmail);
        }

        @Test
        @DisplayName("Should get user by ID")
        void shouldGetById() {
            Long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userMapper.toDto(user)).willReturn(responseDto);

            UserResponseDto result = userService.getById(userId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should throw when user not found by ID")
        void shouldThrowByIdNotFound() {
            Long nonExistingId = 999L;
            given(userRepository.findById(nonExistingId)).willReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.getById(nonExistingId)
            );

            assertThat(exception.getMessage()).contains(nonExistingId.toString());
        }

        @Test
        @DisplayName("Should get all users")
        void shouldGetAll() {
            User user2 = User.builder()
                    .id(2L)
                    .firstName("Jane")
                    .lastName("Doe")
                    .email("jane@example.com")
                    .build();

            UserResponseDto response2 = new UserResponseDto(
                    2L, "Jane", "Doe", "jane@example.com", "05557654321", "ROLE_USER"
            );

            given(userRepository.findAll()).willReturn(List.of(user, user2));
            given(userMapper.toDto(user)).willReturn(responseDto);
            given(userMapper.toDto(user2)).willReturn(response2);

            List<UserResponseDto> result = userService.getAll();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).email()).isEqualTo("mail@example.com");
            assertThat(result.get(1).email()).isEqualTo("jane@example.com");
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void shouldReturnEmptyList() {
            given(userRepository.findAll()).willReturn(Collections.emptyList());

            List<UserResponseDto> result = userService.getAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        private UserUpdateRequestDto updateDto;

        @BeforeEach
        void setUp() {
            updateDto = new UserUpdateRequestDto(
                    "Updated", "Updated", "newMail@example.com", "05559998877"
            );
        }

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUser() {
            UserResponseDto updatedResponseDto = new UserResponseDto(
                    1L, "Updated", "Updated", "newMail@example.com", "05559998877", "ROLE_USER"
            );

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByEmail(updateDto.email())).willReturn(false);
            given(userMapper.toDto(any(User.class))).willReturn(updatedResponseDto);

            UserResponseDto result = userService.update(1L, updateDto);

            assertThat(result).isNotNull();
            assertThat(result.firstName()).isEqualTo(updateDto.firstName());
            assertThat(result.lastName()).isEqualTo(updateDto.lastName());
            assertThat(result.email()).isEqualTo(updateDto.email());
            assertThat(result.phoneNumber()).isEqualTo(updateDto.phoneNumber());
        }

        @Test
        @DisplayName("Should throw when updating to an existing email")
        void shouldThrowWhenEmailTaken() {
            UserUpdateRequestDto updateDto = new UserUpdateRequestDto("Name", "Surname", "taken@example.com", "05551112233");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByEmail("taken@example.com")).willReturn(true);

            ResourceAlreadyExistsException exception = assertThrows(
                    ResourceAlreadyExistsException.class,
                    () -> userService.update(1L, updateDto)
            );

            assertThat(exception.getMessage()).contains("taken@example.com");
            verify(userRepository).existsByEmail("taken@example.com");
        }
    }

    @Nested
    @DisplayName("Change Role Tests")
    class ChangeRoleTests {

        @Test
        @DisplayName("Should change user role")
        void shouldChangeUserRole() {
            // Given
            Long userId = 1L;
            User userWithNewRole = User.builder()
                    .id(userId)
                    .firstName("FirstName")
                    .lastName("LastName")
                    .email("mail@example.com")
                    .password("encoded")
                    .role(librarianRole)
                    .build();

            UserResponseDto updatedResponseDto = new UserResponseDto(
                    userId, "FirstName", "LastName", "mail@example.com", "05551234567", "ROLE_LIBRARIAN"
            );

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(roleRepository.findByName(ERole.ROLE_LIBRARIAN)).willReturn(Optional.of(librarianRole));
            given(userRepository.save(any(User.class))).willReturn(userWithNewRole);
            given(userMapper.toDto(userWithNewRole)).willReturn(updatedResponseDto);

            // When
            UserResponseDto result = userService.changeRole(userId, ERole.ROLE_LIBRARIAN);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.role()).isEqualTo("ROLE_LIBRARIAN");
        }

        @Test
        @DisplayName("Should throw if role not found")
        void shouldThrowWhenRoleMissing() {
            Long userId = 1L;
            ERole nonExistingRole = ERole.ROLE_LIBRARIAN;

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(roleRepository.findByName(nonExistingRole)).willReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.changeRole(userId, nonExistingRole)
            );

            assertThat(exception.getMessage()).contains(nonExistingRole.toString());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user")
        void shouldDeleteUser() {
            given(userRepository.existsById(1L)).willReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            userService.delete(1L);

            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw if user not found on delete")
        void shouldThrowWhenUserNotFoundOnDelete() {
            Long nonExistingId = 999L;
            given(userRepository.existsById(nonExistingId )).willReturn(false);

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.delete(nonExistingId)
            );

            assertThat(exception.getMessage()).contains(nonExistingId.toString());
            verify(userRepository, never()).deleteById(any());
        }
    }
}