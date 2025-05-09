package com.getir.aau.librarymanagementsystem.initializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.getir.aau.librarymanagementsystem.model.dto.BookRequestDto;
import com.getir.aau.librarymanagementsystem.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookInitializer implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final BookService bookService;

    @Override
    public void run(String... args) throws Exception {
        initBooksFromJson();
    }

    private void initBooksFromJson() throws IOException {
        InputStream input = new ClassPathResource("data/books.json").getInputStream();
        List<BookRequestDto> books = objectMapper.readValue(input, new TypeReference<>() {});

        int created = 0;

        for (BookRequestDto dto : books) {
            try {
                bookService.createBook(dto);
                created++;
            } catch (Exception e) {
                log.warn("❌ Failed to create book: {} - {}", dto.title(), e.getMessage());
            }
        }

        log.info("✅ Book initialization completed. Created {} books.", created);
    }
}