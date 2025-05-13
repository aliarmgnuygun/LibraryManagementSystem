package com.getir.aau.librarymanagementsystem.initializer;

import com.getir.aau.librarymanagementsystem.model.entity.ERole;
import com.getir.aau.librarymanagementsystem.model.entity.Role;
import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.repository.RoleRepository;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Initializing default users...");
            createUsers();
        } else {
            log.info("Users already exist. Skipping user initialization.");
        }
    }

    private void createUsers() {
        Role librarianRole = roleRepository.findByName(ERole.ROLE_LIBRARIAN)
                .orElseThrow(() -> new RuntimeException("LIBRARIAN role not found. Did you run RoleInitializer?"));

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("USER role not found. Did you run RoleInitializer?"));

        User librarian = User.builder()
                .firstName("Librarian")
                .lastName("Librarian")
                .email("librarian@gmail.com")
                .password(passwordEncoder.encode("Password123"))
                .phoneNumber("111-111-1111")
                .role(librarianRole)
                .build();

        User user = User.builder()
                .firstName("User")
                .lastName("User")
                .email("user@gmail.com")
                .password(passwordEncoder.encode("Password123"))
                .phoneNumber("222-222-2222")
                .role(userRole)
                .build();

        userRepository.saveAll(List.of(librarian, user));

        log.info("Default users created: librarian@gmail.com / user@gmail.com (password: Password123)");
    }
}