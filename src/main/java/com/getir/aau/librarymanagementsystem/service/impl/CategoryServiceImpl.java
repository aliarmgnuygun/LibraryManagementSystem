package com.getir.aau.librarymanagementsystem.service.impl;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.request.CategoryRequestDto;
import com.getir.aau.librarymanagementsystem.model.dto.response.CategoryResponseDto;
import com.getir.aau.librarymanagementsystem.model.entity.Category;
import com.getir.aau.librarymanagementsystem.model.mapper.CategoryMapper;
import com.getir.aau.librarymanagementsystem.repository.CategoryRepository;
import com.getir.aau.librarymanagementsystem.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDto create(CategoryRequestDto dto) {
        log.info("Creating new category with name: {}", dto.name());

        if (categoryRepository.existsByNameIgnoreCase(dto.name())) {
            throw new ResourceAlreadyExistsException("Category", "name", dto.name());
        }

        Category category = categoryMapper.toEntity(dto);
        Category saved = categoryRepository.save(category);

        log.info("Category created with ID: {}", saved.getId());
        return categoryMapper.toDto(saved);
    }

    @Override
    public CategoryResponseDto update(Long id, CategoryRequestDto dto) {
        log.info("Updating category with ID: {}", id);

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        Category updated = Category.builder()
                .id(existing.getId())
                .name(dto.name())
                .books(existing.getBooks())
                .build();

        Category saved = categoryRepository.save(updated);
        log.info("Category updated with ID: {}", saved.getId());

        return categoryMapper.toDto(saved);
    }

    @Override
    public CategoryResponseDto getById(Long id) {
        log.info("Fetching category by ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        return categoryMapper.toDto(category);
    }

    @Override
    public List<CategoryResponseDto> getAll() {
        log.info("Fetching all categories");
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        log.warn("Deleting category with ID: {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }

        categoryRepository.deleteById(id);
        log.info("Category deleted successfully with ID: {}", id);
    }
}