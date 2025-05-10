package com.getir.aau.librarymanagementsystem.service.impl;

import com.getir.aau.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.dto.response.UserResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.request.UserUpdateRequestDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto create(RegisterRequestDto dto) {
        log.info("Creating user with email: {}", dto.email());

        if (userRepository.existsByEmail(dto.email())) {
            log.warn("User with email {} already exists", dto.email());
            throw new ResourceAlreadyExistsException("User", "email", dto.email());
        }

        Role role = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> {
                    log.error("Default role USER not found");
                    return new ResourceNotFoundException("Role", "name", ERole.ROLE_USER.name());
                });

        User user = userMapper.fromRegisterRequest(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRole(role);

        userRepository.save(user);

        log.info("User created successfully with ID: {}", user.getId());
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getByEmail(String email) {
        log.info("Fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new ResourceNotFoundException("User", "email", email);
                });

        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getById(Long id) {
        log.info("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new ResourceNotFoundException("User", "id", id);
                });

        return userMapper.toDto(user);
    }

    @Override
    public List<UserResponseDto> getAll() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserResponseDto update(Long id, UserUpdateRequestDto dto) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (dto.email() != null && !dto.email().equals(user.getEmail())
                && userRepository.existsByEmail(dto.email())) {
            log.warn("Email {} already in use", dto.email());
            throw new ResourceAlreadyExistsException("User", "email", dto.email());
        }

        userMapper.updateUserFromDto(dto, user);

        log.info("User updated successfully with ID: {}", user.getId());
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto changeRole(Long userId, ERole newRole) {
        log.info("Changing role of user ID: {} to {}", userId, newRole);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Role role = roleRepository.findByName(newRole)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", newRole.name()));

        user.setRole(role);
        User saved = userRepository.save(user);

        log.info("User ID {} role changed to {}", userId, newRole);
        return userMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        log.warn("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email (Optional): {}", email);
        return userRepository.findByEmail(email);
    }
}