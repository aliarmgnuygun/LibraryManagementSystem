package com.getir.aau.librarymanagementsystem.model.dto.response;

/**
 * Represents author data returned to the client
 */
public record AuthorResponseDto(
        Long id,
        String name,
        String description
) {}