package com.getir.aau.librarymanagementsystem.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private Author author;

    @Column(name = "isbn", length = 50, nullable = false, unique = true)
    private String isbn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "description", length = 250, nullable = false)
    private String description;

    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate;

    @Column(nullable = false)
    private String genre;

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
        updateAvailability();
    }

    public void returnBook() {
        numberOfCopies++;
        updateAvailability();
    }

    public void updateAvailability() {
        this.available = this.numberOfCopies > 0;
    }
}