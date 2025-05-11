package com.getir.aau.librarymanagementsystem.initializer;

import com.getir.aau.librarymanagementsystem.model.dto.request.CategoryRequestDto;
import com.getir.aau.librarymanagementsystem.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryInitializer implements CommandLineRunner {

    private final CategoryService categoryService;

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Science Fiction", "Fantasy", "Mystery",
            "Romance", "History", "Philosophy", "Technology"
    );


    @Override
    public void run(String... args) {
        if (categoryService.getAll().isEmpty()) {
            log.info("Initializing categories...");
            initCategories();
        }else {
            log.info("Categories already initialized, skipping...");
        }
    }
    private void initCategories() {
        int created = 0;
        for (String name : DEFAULT_CATEGORIES) {
            created += createCategory(name);
        }
        log.info("✅ Created {} out of {} categories", created, DEFAULT_CATEGORIES.size());
    }

    private int createCategory(String name) {
        try {
            CategoryRequestDto dto = new CategoryRequestDto(name);
            categoryService.create(dto);
            return 1;
        } catch (Exception e) {
            log.warn("Failed to create category: {} - {}", name, e.getMessage());
            return 0;
        }
    }
}