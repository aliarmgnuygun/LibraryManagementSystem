package com.getir.aau.librarymanagementsystem.service.impl;

import com.getir.aau.librarymanagementsystem.model.dto.UserResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.UserUpdateRequestDto;
import com.getir.aau.librarymanagementsystem.model.entity.ERole;
import com.getir.aau.librarymanagementsystem.model.entity.Role;
import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.model.mapper.UserMapper;
import com.getir.aau.librarymanagementsystem.repository.RoleRepository;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.security.auth.dto.RegisterRequestDto;
import com.getir.aau.librarymanagementsystem.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto create(RegisterRequestDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        Role role = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new IllegalArgumentException("Default role USER not found"));

        User user = userMapper.fromRegisterRequest(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRole(role);

        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User with email %s not found".formatted(email)));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id %d not found".formatted(id)));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserResponseDto> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id %d not found".formatted(id)));
        if (dto.email() != null && !dto.email().equals(user.getEmail())
                && userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (dto.role() != null) {
            Role role = roleRepository.findByName(dto.role())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + dto.role()));
            user.setRole(role);
        }

        userMapper.updateUserFromDto(dto, user);
        return userMapper.toDto(user);
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User with id %d not found".formatted(id));
        }
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}