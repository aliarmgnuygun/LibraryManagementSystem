package com.getir.aau.librarymanagementsystem.security.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record AuthResponseDto(
        @JsonProperty("access_token")
        @Schema(description = "JWT access token")
        String accessToken,

        @JsonProperty("refresh_token")
        @Schema(description = "JWT refresh token")
        String refreshToken
) {}