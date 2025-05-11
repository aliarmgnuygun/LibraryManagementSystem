package com.getir.aau.librarymanagementsystem.integration.security;

import com.getir.aau.librarymanagementsystem.model.entity.ERole;
import com.getir.aau.librarymanagementsystem.model.entity.Role;
import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.repository.RoleRepository;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JwtServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String validToken;
    private final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = roleRepository.save(
                Role.builder().name(ERole.ROLE_USER).description("Standard user").build()
        );

        testUser = User.builder()
                .email(TEST_EMAIL)
                .firstName("Test")
                .lastName("User")
                .password(passwordEncoder.encode("password"))
                .role(role)
                .phoneNumber("1234567890")
                .build();

        testUser = userRepository.save(testUser);
        validToken = jwtService.generateToken(testUser);
    }

    @Nested
    @DisplayName("Valid Token Tests")
    class ValidTokenTests {

        @Test
        void shouldAllowWithValidToken() throws Exception {
            mockMvc.perform(get("/api/users/me")
                            .header("Authorization", "Bearer " + validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(TEST_EMAIL));
        }
    }

    @Nested
    @DisplayName("Invalid Token Tests")
    class InvalidTokenTests {

        @Test
        void shouldRejectExpiredToken() throws Exception {
            String expired = jwtService.generateExpiredToken(testUser);

            mockMvc.perform(get("/api/users/me")
                            .header("Authorization", "Bearer " + expired)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldRejectWithInvalidHeaderFormat() throws Exception {
            mockMvc.perform(get("/api/users/me")
                            .header("Authorization", "Invalid " + validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldRejectMalformedToken() throws Exception {
            String malformed = "bad.token";

            mockMvc.perform(get("/api/users/me")
                            .header("Authorization", "Bearer " + malformed)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldRejectTokenWithInvalidSignature() throws Exception {
            String fakeToken = validToken.substring(0, validToken.lastIndexOf('.') + 1) + "invalidSignature";

            mockMvc.perform(get("/api/users/me")
                            .header("Authorization", "Bearer " + fakeToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldProvideProperErrorMessage() throws Exception {
            mockMvc.perform(get("/api/users/me")
                            .header("Authorization", "Bearer invalid.token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("Filter Bypass Tests")
    class AuthBypassTests {

        @Test
        void shouldBypassFilterForAuthPaths() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "test@example.com",
                                      "password": "password"
                                    }
                                    """))
                    .andExpect(status().isOk());
        }
    }
}