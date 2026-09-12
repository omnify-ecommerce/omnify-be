package com.omnify.auth.dto.response;

import java.util.UUID;

public record LoginResponse(
    String accessToken,
    String tokenType,
    Long expiresIn,
    UUID userId,
    String email
) {}
