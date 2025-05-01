package com.getir.aau.librarymanagementsystem.repository;

import com.getir.aau.librarymanagementsystem.model.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

}