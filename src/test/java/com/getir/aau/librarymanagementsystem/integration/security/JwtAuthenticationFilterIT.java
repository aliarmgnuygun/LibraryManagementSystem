package com.getir.aau.librarymanagementsystem.integration.security;

import com.getir.aau.librarymanagementsystem.model.entity.ERole;
import com.getir.aau.librarymanagementsystem.model.entity.Role;
import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.repository.RoleRepository;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.security.jwt.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integration: JwtAuthenticationFilter")
public class JwtAuthenticationFilterIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private User testUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role userRole = roleRepository.save(Role.builder()
                .name(ERole.ROLE_USER)
                .description("Standard user")
                .build());

        testUser = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .password(passwordEncoder.encode("password"))
                .role(userRole)
                .phoneNumber("1234567890")
                .build();

        testUser = userRepository.save(testUser);
        jwtToken = jwtService.generateToken(testUser);
    }

    @Test
    @DisplayName("Should allow access with valid JWT")
    void shouldAllowWithValidToken() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject access with invalid JWT")
    void shouldRejectWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + jwtToken + "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject access with no token")
    void shouldRejectWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should skip filter for /api/auth endpoints")
    void shouldSkipForAuthEndpoints() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() throws Exception {
        String expiredToken = jwtService.generateExpiredToken(testUser);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }
}