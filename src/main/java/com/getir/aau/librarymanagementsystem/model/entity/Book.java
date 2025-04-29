package com.getir.aau.librarymanagementsystem.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "isbn", length = 50, nullable = false, unique = true)
    private String isbn;

    @Column(name = "description", length = 250, nullable = false)
    private String description;

    @Column(name = "publication_date", nullable = false)
    @DateTimeFormat(pattern="dd/MM/yyyy")
    private LocalDate publicationDate;

    @Column(nullable = false)
    private String genre;

    @Getter
    @Column(nullable = false)
    private int numberOfCopies;

    @Column(nullable = false)
    private boolean available = true;

    // Rich domain methods
    public void borrow() {
        if (numberOfCopies <= 0) {
            throw new IllegalStateException("No copies available");
        }
        numberOfCopies--;
        if (numberOfCopies == 0) {
            available = false;
        }
    }

    public void returnBook() {
        numberOfCopies++;
        available = true;
    }
}