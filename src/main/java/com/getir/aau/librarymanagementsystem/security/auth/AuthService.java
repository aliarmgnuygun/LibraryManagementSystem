package com.getir.aau.librarymanagementsystem.security.auth;

import com.getir.aau.librarymanagementsystem.security.auth.dto.AuthRequestDto;
import com.getir.aau.librarymanagementsystem.security.auth.dto.AuthResponseDto;
import com.getir.aau.librarymanagementsystem.security.auth.dto.RegisterRequestDto;

public interface AuthService {

    AuthResponseDto register(RegisterRequestDto request);

    AuthResponseDto login(AuthRequestDto request);

    void logout(String token);
}