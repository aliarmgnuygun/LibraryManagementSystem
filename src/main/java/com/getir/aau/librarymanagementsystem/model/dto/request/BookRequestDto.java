package com.getir.aau.librarymanagementsystem.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * A record representing book creation and update requests
 */
public record BookRequestDto(
        @NotBlank(message = "Title is required")
        String title,

        @NotNull(message = "Author ID is required")
        Long authorId,

        @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "^(?:ISBN(?:-13)?:? )?(?=[0-9]{13}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)97[89][- ]?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9]$",
                message = "Invalid ISBN format")
        String isbn,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotBlank(message = "Description is required")
        String description,

        @JsonFormat(pattern = "dd/MM/yyyy")
        @DateTimeFormat(pattern = "dd/MM/yyyy")
        @NotNull(message = "Publication date is required")
        LocalDate publicationDate,

        @NotBlank(message = "Genre is required")
        String genre,

        @NotNull(message = "Copies are required")
        @PositiveOrZero(message = "Copies must be zero or positive")
        Integer numberOfCopies
) {}