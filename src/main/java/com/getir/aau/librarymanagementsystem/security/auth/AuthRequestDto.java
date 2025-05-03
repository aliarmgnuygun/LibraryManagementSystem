package com.getir.aau.librarymanagementsystem.security.auth;

public record AuthRequestDto(
        String email,
        String password
) {}