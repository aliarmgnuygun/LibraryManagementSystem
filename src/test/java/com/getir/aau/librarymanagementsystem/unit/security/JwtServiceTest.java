package com.getir.aau.librarymanagementsystem.unit.security;

import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;
    private User testUser;
    private static final String TEST_USER_EMAIL = "test@example.com";
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long JWT_EXPIRATION = 86400000; // 1 day
    private static final long REFRESH_EXPIRATION = 604800000; // 7 days


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXPIRATION);

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername(TEST_USER_EMAIL)
                .password("pw")
                .roles("USER")
                .build();

        testUser = User.builder()
                .email(TEST_USER_EMAIL)
                .firstName("Test")
                .lastName("User")
                .password("pw")
                .phoneNumber("1234567890")
                .build();
    }

    @Nested
    @DisplayName("Token Generation")
    class TokenGenerationTests {

        @Test
        void shouldGenerateAccessToken() {
            String token = jwtService.generateToken(userDetails);

            assertNotNull(token);
            assertTrue(jwtService.isTokenValid(token, userDetails));
            assertEquals(TEST_USER_EMAIL, jwtService.extractUsername(token));
        }

        @Test
        void shouldGenerateAccessTokenWithExtraClaims() {
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("role", "USER");

            String token = jwtService.generateToken(extraClaims, userDetails);

            assertNotNull(token);
            assertTrue(jwtService.isTokenValid(token, userDetails));
            assertEquals(TEST_USER_EMAIL, jwtService.extractUsername(token));
        }

        @Test
        void shouldGenerateRefreshToken() {
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            assertNotNull(refreshToken);
            assertTrue(jwtService.isTokenValid(refreshToken, userDetails));
            assertEquals(TEST_USER_EMAIL, jwtService.extractUsername(refreshToken));
        }

        @Test
        void shouldGenerateExpiredToken() {
            String expiredToken = jwtService.generateExpiredToken(testUser);

            assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () ->
                    jwtService.extractClaim(expiredToken, Claims::getExpiration));
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidationTests {

        @Test
        void shouldValidateCorrectToken() {
            String token = jwtService.generateToken(userDetails);
            assertTrue(jwtService.isTokenValid(token, userDetails));
        }

        @Test
        void shouldReturnFalseIfUsernameMismatch() {
            String token = jwtService.generateToken(userDetails);

            UserDetails differentUser = org.springframework.security.core.userdetails.User
                    .withUsername("wrong@example.com")
                    .password("pw")
                    .roles("USER")
                    .build();

            assertFalse(jwtService.isTokenValid(token, differentUser));
        }

        @Test
        void shouldReturnFalseIfTokenExpired() {
            String token = jwtService.generateExpiredToken(testUser);
            assertFalse(jwtService.isTokenValid(token, userDetails));
        }
    }

    @Nested
    @DisplayName("Extraction Tests")
    class ExtractionTests {

        @Test
        void shouldExtractUsernameFromToken() {
            String token = jwtService.generateToken(userDetails);
            String username = jwtService.extractUsername(token);
            assertEquals("test@example.com", username);
        }

        @Test
        void shouldReturnNullForInvalidToken() {
            String invalid = "invalid.token";
            assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername(invalid));
        }

        @Test
        void shouldExtractClaim() {
            String token = jwtService.generateToken(userDetails);
            String username = jwtService.extractClaim(token, Claims::getSubject);
            assertEquals("test@example.com", username);
        }

        @Test
        void shouldExtractAllClaims() {
            String token = jwtService.generateToken(userDetails);
            String subject = jwtService.extractClaim(token, Claims::getSubject);
            Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
            Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

            assertEquals("test@example.com", subject);
            assertNotNull(issuedAt);
            assertNotNull(expiration);
            assertThat(expiration).isAfter(new Date());
        }

        @Test
        void shouldExtractExpiration() {
            String token = jwtService.generateToken(userDetails);
            Date expiration = jwtService.extractClaim(token, Claims::getExpiration);
            assertThat(expiration).isAfter(new Date());
        }
    }

    @Nested
    @DisplayName("Key Handling")
    class KeyTests {

        @Test
        void shouldReturnValidSecretKey() {
            String token = jwtService.generateToken(userDetails);
            assertTrue(jwtService.isTokenValid(token, userDetails));

            String username = jwtService.extractUsername(token);
            assertEquals(TEST_USER_EMAIL, username);
        }
    }
}