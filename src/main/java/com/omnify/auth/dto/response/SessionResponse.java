package com.omnify.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(
    name = "SessionResponse",
    description = "Information about a login session"
)
public class SessionResponse {

    @Schema(
        description = "Login session ID",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private final UUID sessionId;

    @Schema(
        description = "Name of the device used to log in",
        example = "Chrome on Windows 11"
    )
    private final String deviceName;

    @Schema(
        description = "IP address of the login session",
        example = "192.168.1.100"
    )
    private final String ipAddress;

    @Schema(
        description = "Timestamp when the session was last used (ISO-8601)",
        example = "2026-07-30T15:20:35+07:00"
    )
    private final OffsetDateTime lastUsedAt;

    @Schema(
        description = "Timestamp when the session was created (ISO-8601)",
        example = "2026-07-30T08:15:12+07:00"
    )
    private final OffsetDateTime createdAt;

    @Schema(
        description = "Marks whether this is the current session",
        example = "true"
    )
    private final boolean current;
}