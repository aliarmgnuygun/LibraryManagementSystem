package com.getir.aau.librarymanagementsystem.model.dto.response;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String role
) {}
