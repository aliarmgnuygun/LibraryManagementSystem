package com.getir.aau.librarymanagementsystem.security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
        @NotBlank(message = "Email is required!")
        String email,

        @NotBlank(message = "New password is required!")
        @Size(min = 8, message = "Password must be at least 8 characters long!")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
                message = "Password must contain at least one digit, one uppercase letter, one lowercase letter, " +
                        "one special character, and no whitespace")
        String newPassword,

        @NotBlank(message = "Password confirmation is required!")
        @Size(min = 8, message = "Password confirmation must be at least 8 characters long!")
        String confirmPassword
) {}