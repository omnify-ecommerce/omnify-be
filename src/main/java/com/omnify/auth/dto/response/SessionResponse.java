package com.omnify.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(
        name = "SessionResponse",
        description = "Thông tin của một phiên đăng nhập"
)
public class SessionResponse {

    @Schema(
            description = "ID của phiên đăng nhập",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private final UUID sessionId;

    @Schema(
            description = "Tên thiết bị dùng để đăng nhập",
            example = "Chrome on Windows 11"
    )
    private final String deviceName;


    @Schema(
            description = "Địa chỉ IP của phiên đăng nhập",
            example = "192.168.1.100"
    )
    private final String ipAddress;

    @Schema(
            description = "Thời điểm phiên được sử dụng gần nhất (ISO-8601)",
            example = "2026-07-30T15:20:35+07:00"
    )
    private final OffsetDateTime lastUsedAt;

    @Schema(
            description = "Thời điểm tạo phiên đăng nhập (ISO-8601)",
            example = "2026-07-30T08:15:12+07:00"
    )
    private final OffsetDateTime createdAt;

    @Schema(
            description = "Đánh dấu đây có phải là phiên hiện tại hay không",
            example = "true"
    )
    private final boolean current;

    public SessionResponse(
            UUID sessionId,
            String deviceName,
            String ipAddress,
            OffsetDateTime lastUsedAt,
            OffsetDateTime createdAt,
            boolean current) {

        this.sessionId = sessionId;
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
        this.lastUsedAt = lastUsedAt;
        this.createdAt = createdAt;
        this.current = current;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public String getDeviceName() {
        return deviceName;
    }


    public String getIpAddress() {
        return ipAddress;
    }

    public OffsetDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isCurrent() {
        return current;
    }
}