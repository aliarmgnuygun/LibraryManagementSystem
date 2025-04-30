package com.getir.aau.librarymanagementsystem.model.dto;

/**
 * Represents category data returned to the client
 */
public record CategoryResponseDto(
        Long id,
        String name
) {}