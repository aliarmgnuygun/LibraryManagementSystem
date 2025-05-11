package com.getir.aau.librarymanagementsystem.unit.security;

import com.getir.aau.librarymanagementsystem.security.CustomUserDetailsService;
import com.getir.aau.librarymanagementsystem.security.jwt.JwtAuthenticationFilter;
import com.getir.aau.librarymanagementsystem.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    private static class TestableJwtAuthenticationFilter extends JwtAuthenticationFilter {
        public TestableJwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
            super(jwtService, userDetailsService);
        }

        @Override
        public void doFilterInternal(
                @NonNull HttpServletRequest request,
                @NonNull HttpServletResponse response,
                @NonNull FilterChain filterChain
        ) throws ServletException, IOException {
            super.doFilterInternal(request, response, filterChain);
        }
    }
    @Mock private JwtService jwtService;
    @Mock private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private TestableJwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void init() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("/api/auth bypass")
    class AuthPathTests {
        @Test
        @DisplayName("Should skip authentication for auth endpoints")
        void shouldSkipAuth() throws Exception {
            request.setServletPath("/api/auth/login");
            filter.doFilterInternal(request,response,chain);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should skip filter if Authorization header is malformed")
        void shouldSkipWhenAuthorizationHeaderIsInvalidFormat() throws Exception {
            request.addHeader("Authorization", "Token abcdefg");

            filter.doFilterInternal(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should skip authentication if extracted username is null")
        void shouldSkipWhenExtractedUsernameIsNull() throws Exception {
            request.addHeader("Authorization", "Bearer faketoken");
            given(jwtService.extractUsername("faketoken")).willReturn(null);

            filter.doFilterInternal(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        @DisplayName("Should skip authentication if context is already authenticated")
        void shouldSkipWhenAlreadyAuthenticated() throws Exception {
            request.addHeader("Authorization", "Bearer token");
            given(jwtService.extractUsername("token")).willReturn("user@mail.com");

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("user@mail.com", null, List.of())
            );

            filter.doFilterInternal(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("user@mail.com");
        }
    }

    @Nested @DisplayName("Valid token handling")
    class ValidTokenTests {
        @Test @DisplayName("Should set authentication when token valid")
        void shouldAuthenticateWithValidToken() throws Exception {
            String jwt = "token";
            request.addHeader("Authorization","Bearer "+jwt);
            UserDetails details = User.withUsername("user@mail.com").password("pw").roles("USER").build();

            given(jwtService.extractUsername(jwt)).willReturn("user@mail.com");
            given(userDetailsService.loadUserByUsername("user@mail.com")).willReturn(details);
            given(jwtService.isTokenValid(jwt,details)).willReturn(true);

            filter.doFilterInternal(request,response,chain);

            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getPrincipal()).isEqualTo(details);
        }
    }

    @Nested @DisplayName("Invalid / missing header")
    class InvalidHeaderTests {
        @Test @DisplayName("Should continue filter chain when header missing")
        void shouldContinueWhenHeaderMissing() throws Exception {
            filter.doFilterInternal(request,response,chain);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test @DisplayName("Should continue when token invalid")
        void shouldContinueWhenTokenInvalid() throws Exception {
            request.addHeader("Authorization","Bearer bad");
            given(jwtService.extractUsername("bad")).willReturn("user@mail.com");
            given(userDetailsService.loadUserByUsername(anyString())).willReturn(User.withUsername("user@mail.com").password("pw").roles("USER").build());
            given(jwtService.isTokenValid(eq("bad"), any())).willReturn(false);

            filter.doFilterInternal(request,response,chain);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }
}
