package com.getir.aau.librarymanagementsystem.model.dto;

import java.time.LocalDate;

/**
 * A record representing detailed book information returned to the client
 */
public record BookResponseDto(
        Long id,
        String title,
        String isbn,
        String description,
        LocalDate publicationDate,
        String genre,
        Integer numberOfCopies,
        boolean available,
        String authorName,
        String categoryName
) {}