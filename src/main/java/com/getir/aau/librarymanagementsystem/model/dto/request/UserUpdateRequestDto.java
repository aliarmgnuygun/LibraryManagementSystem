package com.getir.aau.librarymanagementsystem.model.dto.request;

public record UserUpdateRequestDto(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String password
) {}