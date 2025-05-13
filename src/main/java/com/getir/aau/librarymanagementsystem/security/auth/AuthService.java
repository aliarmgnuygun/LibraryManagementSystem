package com.getir.aau.librarymanagementsystem.security.auth;

import com.getir.aau.librarymanagementsystem.security.auth.dto.*;

public interface AuthService {

    AuthResponseDto register(RegisterRequestDto request);

    AuthResponseDto login(AuthRequestDto request);

    void logout(String token);

    void changePassword(ChangePasswordRequestDto request);
}