package com.getir.aau.librarymanagementsystem.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(

        @NotBlank(message = "First name is required!")
        String firstName,

        @NotBlank(message = "Last name is required!")
        String lastName,

        @Email(message = "Email is not in valid format!")
        @NotBlank(message = "Email is required!")
        String email,

        @NotBlank(message = "Password is required!")
        String password,

        @NotBlank(message = "Phone number is required!")
        String phoneNumber
) {}