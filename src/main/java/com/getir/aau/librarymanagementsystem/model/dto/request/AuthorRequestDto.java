package com.getir.aau.librarymanagementsystem.model.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents incoming author creation or update requests
 */
public record AuthorRequestDto(

        @NotBlank(message = "Author name is required")
        String name,

        @NotBlank(message = "Description is required")
        String description

) {}