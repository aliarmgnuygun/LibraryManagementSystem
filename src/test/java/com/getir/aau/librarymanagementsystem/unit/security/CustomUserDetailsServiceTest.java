package com.getir.aau.librarymanagementsystem.unit.security;

import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.security.CustomUserDetailsService;
import com.getir.aau.librarymanagementsystem.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private final String TEST_EMAIL = "test@example.com";

    @Nested
    @DisplayName("loadUserByUsername tests")
    class LoadUserTests {

        @Test
        void shouldReturnUserDetails_WhenUserExists() {
            // Arrange
            User user = User.builder().email(TEST_EMAIL).password("password").build();
            when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

            // Act
            UserDetails result = customUserDetailsService.loadUserByUsername(TEST_EMAIL);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(TEST_EMAIL);
        }

        @Test
        void shouldThrowException_WhenUserDoesNotExist() {
            // Arrange
            when(userService.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(TEST_EMAIL))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining(TEST_EMAIL);
        }
    }
}