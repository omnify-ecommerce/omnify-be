package com.omnify.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(
    name = "LoginResponse",
    description = "Information returned after a successful login"
)
public class LoginResponse {

    @Schema(description = "JWT access token used to authenticate API requests",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String accessToken;

    @Schema(description = "Refresh token used to obtain a new access token when it expires",
        example = "e5e5dfe7-4cb8-4ec6-8f33-5f7e8d5d7b2a")
    private final String refreshToken;

    @Schema(description = "ID of the current login session",
        example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID sessionId;

    @Schema(description = "Access token expiration time (in seconds)", example = "900")
    private final long expiresIn;

    @Schema(description = "User ID", example = "a6c4d7a1-2d84-4fcb-9c49-4d5b2d1b9c7f")
    private final UUID userId;

    @Schema(description = "User's role in the system", example = "owner")
    private final String role;
}