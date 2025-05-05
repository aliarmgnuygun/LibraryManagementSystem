package com.getir.aau.librarymanagementsystem.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequestDto(

        @Email(message = "Email is not in valid format!")
        @NotBlank(message = "Email is required!")
        String email,

        @NotBlank(message = "Password is required!")
        String password
) { }