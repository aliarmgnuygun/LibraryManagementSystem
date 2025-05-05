package com.getir.aau.librarymanagementsystem.security.token;

import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.security.auth.dto.AuthResponseDto;

import java.util.Optional;

public interface TokenService {

    void saveUserToken(User user, String jwtToken);

    void revokeAllUserTokens(User user);

    Optional<Token> findByToken(String token);

    void save(Token token);

    AuthResponseDto refreshToken(String refreshToken);
}