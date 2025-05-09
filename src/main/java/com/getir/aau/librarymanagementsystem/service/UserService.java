package com.getir.aau.librarymanagementsystem.service;

import com.getir.aau.librarymanagementsystem.model.dto.response.UserResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.request.UserUpdateRequestDto;
import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.security.auth.dto.RegisterRequestDto;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserResponseDto create(RegisterRequestDto dto);

    UserResponseDto getByEmail(String userEmail);

    UserResponseDto getById(Long id);

    List<UserResponseDto> getAll();

    UserResponseDto updateUser(Long id, UserUpdateRequestDto dto);

    void delete(Long id);

    Optional<User> findByEmail(String email);
}