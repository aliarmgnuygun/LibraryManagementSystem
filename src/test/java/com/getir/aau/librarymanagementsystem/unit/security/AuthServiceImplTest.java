package com.getir.aau.librarymanagementsystem.unit.security;

import com.getir.aau.librarymanagementsystem.model.entity.ERole;
import com.getir.aau.librarymanagementsystem.model.entity.Role;
import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.repository.RoleRepository;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.security.auth.AuthServiceImpl;
import com.getir.aau.librarymanagementsystem.security.auth.dto.AuthRequestDto;
import com.getir.aau.librarymanagementsystem.security.auth.dto.AuthResponseDto;
import com.getir.aau.librarymanagementsystem.security.auth.dto.RegisterRequestDto;
import com.getir.aau.librarymanagementsystem.security.jwt.JwtService;
import com.getir.aau.librarymanagementsystem.security.token.Token;
import com.getir.aau.librarymanagementsystem.security.token.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Implementation Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenService tokenService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDto registerRequest;
    private AuthRequestDto loginRequest;
    private User user;
    private Role userRole;
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encoded-password";

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto("John", "Doe", EMAIL, PASSWORD, "1234567890");
        loginRequest = new AuthRequestDto(EMAIL, PASSWORD);

        userRole = Role.builder()
                .id(1L)
                .name(ERole.ROLE_USER)
                .build();

        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .phoneNumber("1234567890")
                .role(userRole)
                .build();
    }

    @Nested
    @DisplayName("Register Method Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register user successfully when email doesn't exist")
        void shouldRegisterUserWhenEmailDoesNotExist() {
            // Given
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(roleRepository.findByName(any(ERole.class))).willReturn(Optional.of(userRole));
            given(passwordEncoder.encode(anyString())).willReturn(ENCODED_PASSWORD);
            given(userRepository.save(any(User.class))).willReturn(user);
            given(jwtService.generateToken(any(User.class))).willReturn(ACCESS_TOKEN);
            given(jwtService.generateRefreshToken(any(User.class))).willReturn(REFRESH_TOKEN);

            // When
            AuthResponseDto response = authService.register(registerRequest);

            // Then
            assertNotNull(response);
            assertEquals(ACCESS_TOKEN, response.accessToken());
            assertEquals(REFRESH_TOKEN, response.refreshToken());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertEquals(registerRequest.firstName(), savedUser.getFirstName());
            assertEquals(registerRequest.lastName(), savedUser.getLastName());
            assertEquals(registerRequest.email(), savedUser.getEmail());
            assertEquals(ENCODED_PASSWORD, savedUser.getPassword());
            verify(tokenService).saveUserToken(any(User.class), eq(ACCESS_TOKEN));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Given
            given(userRepository.existsByEmail(anyString())).willReturn(true);

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> authService.register(registerRequest));
            assertEquals("Email already in use", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when default role not found")
        void shouldThrowExceptionWhenDefaultRoleNotFound() {
            // Given
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(roleRepository.findByName(any(ERole.class))).willReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> authService.register(registerRequest));
            assertEquals("Default USER role not found", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Login Method Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfullyWithValidCredentials() {
            // Given
            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
            given(jwtService.generateToken(any(User.class))).willReturn(ACCESS_TOKEN);
            given(jwtService.generateRefreshToken(any(User.class))).willReturn(REFRESH_TOKEN);

            // When
            AuthResponseDto response = authService.login(loginRequest);

            // Then
            assertNotNull(response);
            assertEquals(ACCESS_TOKEN, response.accessToken());
            assertEquals(REFRESH_TOKEN, response.refreshToken());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenService).revokeAllUserTokens(user);
            verify(tokenService).saveUserToken(user, ACCESS_TOKEN);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD));
            // When & Then
            UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                    () -> authService.login(loginRequest));
            assertEquals("User not found", exception.getMessage());

            verify(tokenService, never()).revokeAllUserTokens(any(User.class));
            verify(tokenService, never()).saveUserToken(any(User.class), anyString());
        }
    }

    @Nested
    @DisplayName("Logout Method Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully with valid token")
        void shouldLogoutSuccessfullyWithValidToken() {
            // Given
            Token token = Token.builder()
                    .token(ACCESS_TOKEN)
                    .user(user)
                    .expired(false)
                    .revoked(false)
                    .build();

            given(tokenService.findByToken(anyString())).willReturn(Optional.of(token));

            // When
            authService.logout(ACCESS_TOKEN);

            // Then
            ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
            verify(tokenService).save(tokenCaptor.capture());

            Token savedToken = tokenCaptor.getValue();
            assertTrue(savedToken.isExpired());
            assertTrue(savedToken.isRevoked());
        }

        @Test
        @DisplayName("Should do nothing when token is null")
        void shouldDoNothingWhenTokenIsNull() {
            // When
            authService.logout(null);

            // Then
            verify(tokenService, never()).findByToken(anyString());
            verify(tokenService, never()).save(any(Token.class));
        }

        @Test
        @DisplayName("Should do nothing when token is blank")
        void shouldDoNothingWhenTokenIsBlank() {
            // When
            authService.logout("  ");

            // Then
            verify(tokenService, never()).findByToken(anyString());
            verify(tokenService, never()).save(any(Token.class));
        }

        @Test
        @DisplayName("Should do nothing when token not found")
        void shouldDoNothingWhenTokenNotFound() {
            // Given
            given(tokenService.findByToken(anyString())).willReturn(Optional.empty());

            // When
            authService.logout(ACCESS_TOKEN);

            // Then
            verify(tokenService, never()).save(any(Token.class));
        }
    }
}