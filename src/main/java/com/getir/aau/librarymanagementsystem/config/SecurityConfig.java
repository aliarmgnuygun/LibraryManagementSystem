package com.getir.aau.librarymanagementsystem.config;

import com.getir.aau.librarymanagementsystem.security.exception.JwtAuthenticationEntryPoint;
import com.getir.aau.librarymanagementsystem.security.exception.RestAccessDeniedHandler;
import com.getir.aau.librarymanagementsystem.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/error",
                                "/api/auth/**"
                        ).permitAll()
                        // Authentication
                        .requestMatchers(HttpMethod.POST, "/api/auth/change-password").hasAnyRole("USER", "LIBRARIAN")
                        .requestMatchers("/api/auth/**").permitAll()

                        // Book Management
                        .requestMatchers(HttpMethod.GET, "/api/books/unavailable").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("USER", "LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("LIBRARIAN")

                        // User Management
                        .requestMatchers(HttpMethod.GET, "/api/users/me").hasAnyRole("USER", "LIBRARIAN")
                        .requestMatchers("/api/users/**").hasRole("LIBRARIAN")

                        // Category Management
                        .requestMatchers("/api/categories/**").hasRole("LIBRARIAN")

                        // Author Management
                        .requestMatchers("/api/authors/**").hasRole("LIBRARIAN")

                        // Borrow Management
                        .requestMatchers(HttpMethod.GET, "/api/borrow-records").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/api/borrow-records").hasAnyRole("USER", "LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrow-records/filter").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrow-records/check-eligibility/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrow-records/book-availability/**").hasRole("LIBRARIAN")

                        .requestMatchers(HttpMethod.GET, "/api/borrow-records/{id}").hasAnyRole("USER", "LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrow-records/user/{userId}").hasAnyRole("USER", "LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrow-records/user/{userId}/active").hasAnyRole("USER", "LIBRARIAN")


                        // Borrow Item Management
                        .requestMatchers(HttpMethod.PUT, "/api/borrow-items/{itemId}/return").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrow-items/user/{userId}").hasAnyRole("USER", "LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrow-items/user/{userId}/count-active").hasAnyRole("USER", "LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrow-items/user/{userId}/exist-overdue").hasAnyRole("USER", "LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrow-items/overdue").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrow-items/date-range").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrow-items/book/{bookId}").hasRole("LIBRARIAN")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}