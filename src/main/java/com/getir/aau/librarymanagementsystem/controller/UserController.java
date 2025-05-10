package com.getir.aau.librarymanagementsystem.controller;

import com.getir.aau.librarymanagementsystem.exception.ExceptionResult;
import com.getir.aau.librarymanagementsystem.model.dto.response.UserResponseDto;
import com.getir.aau.librarymanagementsystem.model.dto.request.UserUpdateRequestDto;
import com.getir.aau.librarymanagementsystem.model.entity.ERole;
import com.getir.aau.librarymanagementsystem.security.auth.dto.RegisterRequestDto;
import com.getir.aau.librarymanagementsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User creation, update, deletion, and retrieval operations")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create a new user (default role: USER)", responses = {
            @ApiResponse(responseCode = "200", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already in use",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
    })
    @PostMapping
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody RegisterRequestDto dto) {
        return ResponseEntity.ok(userService.create(dto));
    }

    @Operation(summary = "Get current user's own information", responses = {
            @ApiResponse(responseCode = "200", description = "User details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class))),
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMe(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    @Operation(summary = "Get user by email", responses = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
    })
    @GetMapping("/email")
    public ResponseEntity<UserResponseDto> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    @Operation(summary = "Get user by ID", responses = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(summary = "Get all users", responses = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @Operation(summary = "Update a user", responses = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class))),
            @ApiResponse(responseCode = "400", description = "Email already in use",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDto dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @Operation(summary = "Change a user's role", description = "Change the role of an existing user. Only LIBRARIAN or ADMIN users should be allowed.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User role updated successfully",
                            content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "User or Role not found",
                            content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
            })
    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponseDto> changeRole(
            @PathVariable Long id,
            @RequestParam ERole role) {
        return ResponseEntity.ok(userService.changeRole(id, role));
    }

    @Operation(summary = "Delete a user", responses = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResult.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}