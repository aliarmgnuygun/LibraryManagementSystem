package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    void save(User user);
    List<User> findAll();
    void deleteById(Long id);
}