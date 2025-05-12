package com.getir.aau.librarymanagementsystem.unit.controller;

import com.getir.aau.librarymanagementsystem.controller.AuthController;
import com.getir.aau.librarymanagementsystem.security.auth.AuthService;
import com.getir.aau.librarymanagementsystem.security.auth.dto.AuthRequestDto;
import com.getir.aau.librarymanagementsystem.security.auth.dto.AuthResponseDto;
import com.getir.aau.librarymanagementsystem.security.auth.dto.RegisterRequestDto;
import com.getir.aau.librarymanagementsystem.security.token.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequestDto registerRequest;
    private AuthRequestDto loginRequest;
    private AuthResponseDto authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto(
                "John",
                "Doe",
                "john@example.com",
                "password123",
                "1234567890"
        );

        loginRequest = new AuthRequestDto("john@example.com", "password123");

        authResponse = new AuthResponseDto("access-token", "refresh-token");
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("should return 201 CREATED when valid input is provided")
        void register_ShouldReturnCreatedStatus() {
            when(authService.register(any(RegisterRequestDto.class))).thenReturn(authResponse);

            ResponseEntity<AuthResponseDto> response = authController.register(registerRequest);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("access-token", response.getBody().accessToken());
            assertEquals("refresh-token", response.getBody().refreshToken());
            verify(authService, times(1)).register(registerRequest);
        }

        @Test
        @DisplayName("should handle validation errors")
        void register_ShouldHandleValidationErrors() {
            RegisterRequestDto invalidRequest = new RegisterRequestDto("", "", "", "", "");

            when(authService.register(invalidRequest)).thenThrow(new RuntimeException("Validation failed"));

            assertThrows(RuntimeException.class, () -> authController.register(invalidRequest));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @Test
        @DisplayName("should return 200 OK when credentials are valid")
        void login_ShouldReturnOkStatus() {
            when(authService.login(any(AuthRequestDto.class))).thenReturn(authResponse);

            ResponseEntity<AuthResponseDto> response = authController.login(loginRequest);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("access-token", response.getBody().accessToken());
            assertEquals("refresh-token", response.getBody().refreshToken());
            verify(authService, times(1)).login(loginRequest);
        }

        @Test
        @DisplayName("should handle invalid credentials")
        void login_ShouldHandleInvalidCredentials() {
            AuthRequestDto invalidCredentials = new AuthRequestDto("invalid@example.com", "wrongPassword");

            when(authService.login(invalidCredentials)).thenThrow(new RuntimeException("Invalid credentials"));

            assertThrows(RuntimeException.class, () -> authController.login(invalidCredentials));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("should return 200 OK when logout is successful")
        void logout_ShouldReturnOkStatus() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer access-token");

            doNothing().when(authService).logout("access-token");

            ResponseEntity<Void> response = authController.logout(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(authService, times(1)).logout("access-token");
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when Authorization header is missing")
        void logout_ShouldReturnBadRequest_WhenAuthorizationHeaderIsMissing() {
            MockHttpServletRequest request = new MockHttpServletRequest();

            ResponseEntity<Void> response = authController.logout(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verifyNoInteractions(authService);
        }


        @Test
        @DisplayName("should return 400 BAD REQUEST when Authorization header doesn't start with Bearer")
        void logout_ShouldReturnBadRequest_WhenAuthorizationHeaderDoesntStartWithBearer() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Basic access-token");

            ResponseEntity<Void> response = authController.logout(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when Authorization header is empty")
        void logout_ShouldReturnBadRequest_WhenAuthorizationHeaderIsEmpty() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "");

            ResponseEntity<Void> response = authController.logout(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verifyNoInteractions(authService);
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class RefreshTokenTests {

        @Test
        @DisplayName("should return 200 OK with new tokens when refresh token is valid")
        void refreshToken_ShouldReturnOkStatus() {
            String refreshTokenValue = "refresh-token";

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + refreshTokenValue);

            when(tokenService.refreshToken(refreshTokenValue)).thenReturn(authResponse);

            ResponseEntity<AuthResponseDto> response = authController.refreshToken(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("access-token", response.getBody().accessToken());
            verify(tokenService, times(1)).refreshToken(refreshTokenValue);
        }

        @Test
        @DisplayName("should handle invalid refresh token")
        void refreshToken_ShouldHandleInvalidToken() {
            String invalidToken = "invalid-refresh-token";

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer " + invalidToken);

            when(tokenService.refreshToken(invalidToken)).thenThrow(new RuntimeException("Invalid refresh token"));

            assertThrows(RuntimeException.class, () -> authController.refreshToken(request));
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when Authorization header is missing")
        void refreshToken_ShouldReturnBadRequest_WhenAuthorizationHeaderIsMissing() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            // Authorization header yok

            ResponseEntity<AuthResponseDto> response = authController.refreshToken(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verifyNoInteractions(tokenService);
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when Authorization header doesn't start with Bearer")
        void refreshToken_ShouldReturnBadRequest_WhenAuthorizationHeaderDoesntStartWithBearer() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Basic refresh-token");

            ResponseEntity<AuthResponseDto> response = authController.refreshToken(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verifyNoInteractions(tokenService);
        }

        @Test
        @DisplayName("should return 400 BAD REQUEST when Authorization header is empty")
        void refreshToken_ShouldReturnBadRequest_WhenAuthorizationHeaderIsEmpty() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "");

            ResponseEntity<AuthResponseDto> response = authController.refreshToken(request);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verifyNoInteractions(tokenService);
        }
    }
}