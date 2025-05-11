package com.getir.aau.librarymanagementsystem.initializer;

import com.getir.aau.librarymanagementsystem.model.dto.request.AuthorRequestDto;
import com.getir.aau.librarymanagementsystem.service.AuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorInitializer implements CommandLineRunner {

    private final AuthorService authorService;

    private static final List<AuthorRequestDto> DEFAULT_AUTHORS = List.of(
            new AuthorRequestDto("George Orwell", "British writer known for '1984' and 'Animal Farm'."),
            new AuthorRequestDto("Jane Austen", "English novelist known for romantic fiction set among the British landed gentry."),
            new AuthorRequestDto("J.K. Rowling", "British author best known for the Harry Potter fantasy series."),
            new AuthorRequestDto("Isaac Asimov", "American writer and professor of biochemistry, known for science fiction and popular science."),
            new AuthorRequestDto("Agatha Christie", "English writer known for her detective novels featuring Hercule Poirot and Miss Marple."),
            new AuthorRequestDto("Fyodor Dostoevsky", "Russian novelist and philosopher, author of 'Crime and Punishment'."),
            new AuthorRequestDto("J.R.R. Tolkien", "English writer, poet and academic, best known for 'The Lord of the Rings'."),
            new AuthorRequestDto("Albert Camus", "French philosopher, author and journalist, known for his works on absurdism."),
            new AuthorRequestDto("Dan Brown", "American author known for thriller novels including 'The Da Vinci Code'."),
            new AuthorRequestDto("Mary Shelley", "English novelist who wrote 'Frankenstein'; pioneer of science fiction.")
    );

    @Override
    public void run(String... args) {
        if (authorService.getAll().isEmpty()) {
            log.info("Initializing authors...");
            initAuthors();
        } else {
            log.info("Authors already exist in the database. Skipping initialization.");
        }
    }

    private void initAuthors() {
        int created = 0;
        for (AuthorRequestDto author : DEFAULT_AUTHORS) {
            created += createAuthor(author);
        }
        log.info("✅ Created {} out of {} authors", created, DEFAULT_AUTHORS.size());
    }

    private int createAuthor(AuthorRequestDto author) {
        try {
            authorService.create(author);
            return 1;
        } catch (Exception e) {
            log.warn("Failed to create author: {} - {}", author.name(), e.getMessage());
            return 0;
        }
    }
}