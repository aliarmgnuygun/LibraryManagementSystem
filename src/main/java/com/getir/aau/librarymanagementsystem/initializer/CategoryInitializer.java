package com.getir.aau.librarymanagementsystem.initializer;

import com.getir.aau.librarymanagementsystem.model.entity.Category;
import com.getir.aau.librarymanagementsystem.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        initCategories();
    }

    private void initCategories() {
        createCategoryIfNotExists("Science Fiction");
        createCategoryIfNotExists("Fantasy");
        createCategoryIfNotExists("Mystery");
        createCategoryIfNotExists("Romance");
        createCategoryIfNotExists("History");
        createCategoryIfNotExists("Philosophy");
        createCategoryIfNotExists("Technology");
    }

    private void createCategoryIfNotExists(String name) {
        categoryRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Category category = Category.builder()
                    .name(name)
                    .build();
            return categoryRepository.save(category);
        });
    }
}