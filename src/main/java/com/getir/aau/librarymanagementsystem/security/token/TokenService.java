package com.getir.aau.librarymanagementsystem.security.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.security.auth.AuthResponseDto;
import com.getir.aau.librarymanagementsystem.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final TokenRepository tokenRepository;

    public void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
        log.info("Access token saved for user: {}", user.getEmail());
    }

    public void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            log.info("No valid tokens found to revoke for user: {}", user.getEmail());
            return;
        }

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
        log.info("All existing tokens revoked for user: {}", user.getEmail());
    }

    public void handleRefreshToken(
            HttpServletRequest request,
            HttpServletResponse response,
            JwtService jwtService,
            UserRepository userRepository
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Refresh request without proper Authorization header");
            return;
        }

        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                log.warn("Refresh token tried for non-existent user: {}", userEmail);
                return;
            }

            if (jwtService.isTokenValid(refreshToken, user)) {
                var newAccessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, newAccessToken);

                var authResponse = new AuthResponseDto(newAccessToken, refreshToken);
                log.info("Refresh token used successfully. New access token issued for user: {}", userEmail);

                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            } else {
                log.warn("Invalid refresh token used for user: {}", userEmail);
            }
        } else {
            log.warn("Could not extract userEmail from refresh token.");
        }
    }
}