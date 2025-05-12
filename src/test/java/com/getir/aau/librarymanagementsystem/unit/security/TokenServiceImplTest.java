package com.getir.aau.librarymanagementsystem.unit.security;

import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.security.auth.dto.AuthResponseDto;
import com.getir.aau.librarymanagementsystem.security.jwt.JwtService;
import com.getir.aau.librarymanagementsystem.security.token.Token;
import com.getir.aau.librarymanagementsystem.security.token.TokenRepository;
import com.getir.aau.librarymanagementsystem.security.token.TokenServiceImpl;
import com.getir.aau.librarymanagementsystem.security.token.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Token Service Implementation Tests")
class TokenServiceImplTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Captor
    private ArgumentCaptor<Token> tokenCaptor;

    @Captor
    private ArgumentCaptor<List<Token>> tokensCaptor;

    private Token token;
    private User user;
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIn0.8Vr9S7DJI";
    private static final String REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiZXhwIjoxNjE1MzI3fQ.5Xyx_8";
    private static final String USER_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email(USER_EMAIL)
                .firstName("Test")
                .lastName("User")
                .build();

        token = Token.builder()
                .id(1L)
                .token(JWT_TOKEN)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .user(user)
                .build();
    }

    @Nested
    @DisplayName("findByToken Method Tests")
    class FindByTokenTests {

        @Test
        @DisplayName("Should return token when found in repository")
        void shouldReturnTokenWhenFound() {
            // Given
            when(tokenRepository.findByToken(JWT_TOKEN)).thenReturn(Optional.of(token));

            // When
            Optional<Token> result = tokenService.findByToken(JWT_TOKEN);

            // Then
            assertTrue(result.isPresent());
            assertThat(result.get()).isEqualTo(token);
            verify(tokenRepository).findByToken(JWT_TOKEN);
        }

        @Test
        @DisplayName("Should return empty optional when token not found")
        void shouldReturnEmptyOptionalWhenTokenNotFound() {
            // Given
            when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

            // When
            Optional<Token> result = tokenService.findByToken("invalid-token");

            // Then
            assertFalse(result.isPresent());
            verify(tokenRepository).findByToken("invalid-token");
        }
    }

    @Nested
    @DisplayName("save Method Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save token to repository")
        void shouldSaveTokenToRepository() {
            // When
            tokenService.save(token);

            // Then
            verify(tokenRepository).save(token);
        }
    }

    @Nested
    @DisplayName("saveUserToken Method Tests")
    class SaveUserTokenTests {

        @Test
        @DisplayName("Should create and save new token for user")
        void shouldCreateAndSaveNewTokenForUser() {
            // When
            tokenService.saveUserToken(user, JWT_TOKEN);

            // Then
            verify(tokenRepository).save(tokenCaptor.capture());

            Token savedToken = tokenCaptor.getValue();
            assertThat(savedToken.getUser()).isEqualTo(user);
            assertThat(savedToken.getToken()).isEqualTo(JWT_TOKEN);
            assertThat(savedToken.getTokenType()).isEqualTo(TokenType.BEARER);
            assertFalse(savedToken.isExpired());
            assertFalse(savedToken.isRevoked());
        }
    }

    @Nested
    @DisplayName("revokeAllUserTokens Method Tests")
    class RevokeAllUserTokensTests {

        @Test
        @DisplayName("Should revoke all valid tokens for user")
        void shouldRevokeAllValidTokensForUser() {
            // Given
            Token token1 = Token.builder()
                    .id(1L)
                    .token("token1")
                    .user(user)
                    .expired(false)
                    .revoked(false)
                    .build();

            Token token2 = Token.builder()
                    .id(2L)
                    .token("token2")
                    .user(user)
                    .expired(false)
                    .revoked(false)
                    .build();

            List<Token> validTokens = Arrays.asList(token1, token2);
            when(tokenRepository.findAllValidTokenByUser(user.getId())).thenReturn(validTokens);

            // When
            tokenService.revokeAllUserTokens(user);

            // Then
            verify(tokenRepository).findAllValidTokenByUser(user.getId());
            verify(tokenRepository).saveAll(tokensCaptor.capture());

            List<Token> savedTokens = tokensCaptor.getValue();
            assertThat(savedTokens).hasSize(2);
            assertThat(savedTokens).allMatch(Token::isExpired);
            assertThat(savedTokens).allMatch(Token::isRevoked);
        }

        @Test
        @DisplayName("Should do nothing when no valid tokens found")
        void shouldDoNothingWhenNoValidTokensFound() {
            // Given
            when(tokenRepository.findAllValidTokenByUser(anyLong())).thenReturn(Collections.emptyList());

            // When
            tokenService.revokeAllUserTokens(user);

            // Then
            verify(tokenRepository).findAllValidTokenByUser(user.getId());
            verify(tokenRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("refreshToken Method Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should generate new access token when refresh token is valid")
        void shouldGenerateNewAccessTokenWhenRefreshTokenIsValid() {
            // Given
            when(jwtService.extractUsername(REFRESH_TOKEN)).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(jwtService.isTokenValid(REFRESH_TOKEN, user)).thenReturn(true);
            when(jwtService.generateToken(user)).thenReturn(JWT_TOKEN);
            when(tokenRepository.findAllValidTokenByUser(user.getId())).thenReturn(Collections.emptyList());

            // When
            AuthResponseDto response = tokenService.refreshToken(REFRESH_TOKEN);

            // Then
            assertNotNull(response);
            assertEquals(JWT_TOKEN, response.accessToken());
            assertEquals(REFRESH_TOKEN, response.refreshToken());

            verify(jwtService).extractUsername(REFRESH_TOKEN);
            verify(userRepository).findByEmail(USER_EMAIL);
            verify(jwtService).isTokenValid(REFRESH_TOKEN, user);
            verify(jwtService).generateToken(user);
            verify(tokenRepository).save(any(Token.class));
        }

        @Test
        @DisplayName("Should throw exception when refresh token does not contain username")
        void shouldThrowExceptionWhenRefreshTokenDoesNotContainUsername() {
            // Given
            when(jwtService.extractUsername(anyString())).thenReturn(null);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tokenService.refreshToken(REFRESH_TOKEN));
            assertEquals("Invalid refresh token", exception.getMessage());

            verify(jwtService).extractUsername(REFRESH_TOKEN);
            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(jwtService.extractUsername(REFRESH_TOKEN)).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tokenService.refreshToken(REFRESH_TOKEN));
            assertEquals("User not found with email: " + USER_EMAIL, exception.getMessage());

            verify(jwtService).extractUsername(REFRESH_TOKEN);
            verify(userRepository).findByEmail(USER_EMAIL);
            verify(jwtService, never()).isTokenValid(anyString(), any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when refresh token is invalid")
        void shouldThrowExceptionWhenRefreshTokenIsInvalid() {
            // Given
            when(jwtService.extractUsername(REFRESH_TOKEN)).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(jwtService.isTokenValid(REFRESH_TOKEN, user)).thenReturn(false);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tokenService.refreshToken(REFRESH_TOKEN));
            assertEquals("Refresh token is not valid", exception.getMessage());

            verify(jwtService).extractUsername(REFRESH_TOKEN);
            verify(userRepository).findByEmail(USER_EMAIL);
            verify(jwtService).isTokenValid(REFRESH_TOKEN, user);
            verify(jwtService, never()).generateToken(any(User.class));
        }
    }
}