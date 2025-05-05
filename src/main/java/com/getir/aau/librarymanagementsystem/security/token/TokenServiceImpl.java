package com.getir.aau.librarymanagementsystem.security.token;

import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.security.auth.dto.AuthResponseDto;
import com.getir.aau.librarymanagementsystem.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public Optional<Token> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public void save(Token token) {
        tokenRepository.save(token);
    }

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

    @Override
    public AuthResponseDto refreshToken(String refreshToken) {
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            log.warn("Could not extract userEmail from refresh token.");
            throw new RuntimeException("Invalid refresh token");
        }

        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            log.warn("Invalid refresh token used for user: {}", userEmail);
            throw new RuntimeException("Refresh token is not valid");
        }

        var newAccessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, newAccessToken);

        log.info("Refresh token used successfully. New access token issued for user: {}", userEmail);

        return new AuthResponseDto(newAccessToken, refreshToken);
    }
}