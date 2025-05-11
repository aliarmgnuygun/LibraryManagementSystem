package com.getir.aau.librarymanagementsystem.integration.security;

import com.getir.aau.librarymanagementsystem.model.entity.ERole;
import com.getir.aau.librarymanagementsystem.model.entity.Role;
import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.repository.RoleRepository;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CustomUserDetailsServiceIT {

    @Autowired private CustomUserDetailsService customUserDetailsService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final String TEST_EMAIL = "integration@example.com";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = roleRepository.save(Role.builder()
                .name(ERole.ROLE_USER)
                .description("Standard User")
                .build());

        User user = User.builder()
                .email(TEST_EMAIL)
                .firstName("Integration")
                .lastName("Test")
                .password(passwordEncoder.encode("password"))
                .role(role)
                .phoneNumber("1234567890")
                .build();

        userRepository.save(user);
    }

    @Test
    void shouldLoadUserDetailsSuccessfully() {
        var userDetails = customUserDetailsService.loadUserByUsername(TEST_EMAIL);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(TEST_EMAIL);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        assertThatThrownBy(() ->
                customUserDetailsService.loadUserByUsername("notfound@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("notfound@example.com");
    }
}