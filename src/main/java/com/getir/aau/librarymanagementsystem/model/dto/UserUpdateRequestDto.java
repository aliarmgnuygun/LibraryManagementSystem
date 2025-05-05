package com.getir.aau.librarymanagementsystem.model.dto;

import com.getir.aau.librarymanagementsystem.model.entity.ERole;

public record UserUpdateRequestDto(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        ERole role
) {}