package com.getir.aau.librarymanagementsystem.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents incoming category creation or update requests
 */
public record CategoryRequestDto(

        @NotBlank(message = "Name is required")
        String name
) {}