package com.getir.aau.librarymanagementsystem.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(

        @NotBlank(message = "First name is required!")
        String firstName,

        @NotBlank(message = "Last name is required!")
        String lastName,

        @Email(message = "Email is not in valid format!")
        @NotBlank(message = "Email is required!")
        String email,

        @NotBlank(message = "Password is required!")
        @Size(min = 8, message = "Password must be at least 8 characters long!")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
                message = "Password must contain at least one digit, one uppercase letter, one lowercase letter, " +
                        "one special character, and no whitespace")
        String password,

        @NotBlank(message = "Phone number is required!")
        @Pattern(regexp = "^[+]?[0-9]{10,13}$", message = "Phone number is not in valid format!")
        String phoneNumber
) {}