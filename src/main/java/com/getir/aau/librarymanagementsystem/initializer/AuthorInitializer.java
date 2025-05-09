package com.getir.aau.librarymanagementsystem.initializer;

import com.getir.aau.librarymanagementsystem.model.entity.Author;
import com.getir.aau.librarymanagementsystem.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;

    @Override
    public void run(String... args) {
        initAuthors();
    }

    private void initAuthors() {
        createAuthorIfNotExists("George Orwell", "British writer known for '1984' and 'Animal Farm'.");
        createAuthorIfNotExists("Jane Austen", "English novelist known for romantic fiction set among the British landed gentry.");
        createAuthorIfNotExists("J.K. Rowling", "British author best known for the Harry Potter fantasy series.");
        createAuthorIfNotExists("Isaac Asimov", "American writer and professor of biochemistry, known for science fiction and popular science.");
        createAuthorIfNotExists("Agatha Christie", "English writer known for her detective novels featuring Hercule Poirot and Miss Marple.");
        createAuthorIfNotExists("Fyodor Dostoevsky", "Russian novelist and philosopher, author of 'Crime and Punishment'.");
        createAuthorIfNotExists("J.R.R. Tolkien", "English writer, poet and academic, best known for 'The Lord of the Rings'.");
        createAuthorIfNotExists("Albert Camus", "French philosopher, author and journalist, known for his works on absurdism.");
        createAuthorIfNotExists("Dan Brown", "American author known for thriller novels including 'The Da Vinci Code'.");
        createAuthorIfNotExists("Mary Shelley", "English novelist who wrote 'Frankenstein'; pioneer of science fiction.");
    }

    private void createAuthorIfNotExists(String name, String description) {
        authorRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Author author = Author.builder()
                    .name(name)
                    .description(description)
                    .build();
            return authorRepository.save(author);
        });
    }
}