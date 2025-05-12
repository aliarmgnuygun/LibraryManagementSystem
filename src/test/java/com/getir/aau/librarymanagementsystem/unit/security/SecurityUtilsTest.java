package com.getir.aau.librarymanagementsystem.unit.security;

import com.getir.aau.librarymanagementsystem.exception.ResourceNotFoundException;
import com.getir.aau.librarymanagementsystem.model.entity.User;
import com.getir.aau.librarymanagementsystem.repository.UserRepository;
import com.getir.aau.librarymanagementsystem.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityUtils securityUtils;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("getCurrentUserEmail Method Tests")
    class GetCurrentUserEmailTests {

        @Test
        @DisplayName("Should return email when authenticated with UserDetails")
        void shouldReturnEmailWhenAuthenticatedWithUserDetails() {
            // Given
            String email = "test@example.com";
            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn(email);

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            // When
            String result = securityUtils.getCurrentUserEmail();

            // Then
            assertEquals(email, result);
        }

        @Test
        @DisplayName("Should return email when authenticated with String principal")
        void shouldReturnEmailWhenAuthenticatedWithStringPrincipal() {
            // Given
            String email = "test@example.com";
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            // When
            String result = securityUtils.getCurrentUserEmail();

            // Then
            assertEquals(email, result);
        }

        @Test
        @DisplayName("Should throw exception when not authenticated")
        void shouldThrowExceptionWhenNotAuthenticated() {
            // Given - No authentication set

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> securityUtils.getCurrentUserEmail());
            assertEquals("No authenticated user found", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when principal type not supported")
        void shouldThrowExceptionWhenPrincipalTypeNotSupported() {
            // Given
            Object unsupportedPrincipal = new Object();
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    unsupportedPrincipal, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            // When & Then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> securityUtils.getCurrentUserEmail());
            assertEquals("User principal type not supported: " + unsupportedPrincipal.getClass(),
                    exception.getMessage());
        }
    }

    @Nested
    @DisplayName("getCurrentUser Method Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return user when authenticated and user found in repository")
        void shouldReturnUserWhenAuthenticatedAndUserFoundInRepository() {
            // Given
            String email = "test@example.com";
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            User expectedUser = User.builder()
                    .id(1L)
                    .email(email)
                    .build();
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedUser));

            // When
            User result = securityUtils.getCurrentUser();

            // Then
            assertSame(expectedUser, result);
            verify(userRepository).findByEmail(email);
        }

        @Test
        @DisplayName("Should throw exception when user not found in repository")
        void shouldThrowExceptionWhenUserNotFoundInRepository() {
            // Given
            String email = "nonexistent@example.com";
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // When & Then
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                    () -> securityUtils.getCurrentUser());
            assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
            verify(userRepository).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("hasRole Method Tests")
    class HasRoleTests {

        @Test
        @DisplayName("Should return true when user has requested role")
        void shouldReturnTrueWhenUserHasRequestedRole() {
            // Given
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "test@example.com", null,
                    List.of(new SimpleGrantedAuthority("ROLE_LIBRARIAN")));
            SecurityContextHolder.getContext().setAuthentication(auth);

            // When
            boolean hasRole = securityUtils.hasRole("LIBRARIAN");

            // Then
            assertTrue(hasRole);
        }

        @Test
        @DisplayName("Should return false when user doesn't have requested role")
        void shouldReturnFalseWhenUserDoesNotHaveRequestedRole() {
            // Given
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "test@example.com", null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);

            // When
            boolean hasRole = securityUtils.hasRole("LIBRARIAN");

            // Then
            assertFalse(hasRole);
        }

        @Test
        @DisplayName("Should return false when no authentication")
        void shouldReturnFalseWhenNoAuthentication() {
            // Given - No authentication set

            // When
            boolean hasRole = securityUtils.hasRole("LIBRARIAN");

            // Then
            assertFalse(hasRole);
        }
    }

    @Nested
    @DisplayName("checkAccessPermissionForUser Method Tests")
    class CheckAccessPermissionForUserTests {

        @Test
        @DisplayName("Should pass when current user is accessing their own data")
        void shouldPassWhenCurrentUserIsAccessingTheirOwnData() {
            // Given
            String email = "user@example.com";
            Long userId = 1L;

            User currentUser = User.builder().id(userId).email(email).build();
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(currentUser));

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    email, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);

            // When & Then - Should not throw exception
            assertDoesNotThrow(() -> securityUtils.checkAccessPermissionForUser(userId));
        }

        @Test
        @DisplayName("Should pass when current user is librarian accessing another user's data")
        void shouldPassWhenCurrentUserIsLibrarianAccessingAnotherUsersData() {
            // Given
            String librarianEmail = "librarian@example.com";
            Long librarianId = 1L;
            Long targetUserId = 2L;

            User librarian = User.builder().id(librarianId).email(librarianEmail).build();
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(librarian));

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    librarianEmail, null, List.of(new SimpleGrantedAuthority("ROLE_LIBRARIAN")));
            SecurityContextHolder.getContext().setAuthentication(auth);

            // When & Then - Should not throw exception
            assertDoesNotThrow(() -> securityUtils.checkAccessPermissionForUser(targetUserId));
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when non-librarian user accessing another user's data")
        void shouldThrowAccessDeniedExceptionWhenNonLibrarianUserAccessingAnotherUsersData() {
            // Given
            String userEmail = "user@example.com";
            Long userId = 1L;
            Long targetUserId = 2L;

            User user = User.builder().id(userId).email(userEmail).build();
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userEmail, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);

            // When & Then
            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                    () -> securityUtils.checkAccessPermissionForUser(targetUserId));
            assertEquals("You are not allowed to access this resource.", exception.getMessage());
        }
    }
}