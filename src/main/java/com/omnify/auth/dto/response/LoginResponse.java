package com.omnify.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(
        name = "LoginResponse",
        description = "Thông tin trả về sau khi đăng nhập thành công"
)
public class LoginResponse {

    @Schema(description = "JWT Access Token dùng để xác thực các API",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String accessToken;

    @Schema(description = "Refresh Token dùng để lấy Access Token mới khi Access Token hết hạn",
            example = "e5e5dfe7-4cb8-4ec6-8f33-5f7e8d5d7b2a")
    private final String refreshToken;

    @Schema(description = "ID của phiên đăng nhập hiện tại",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID sessionId;

    @Schema(description = "Thời gian hết hạn của Access Token (đơn vị: giây)", example = "900")
    private final long expiresIn;

    @Schema(description = "ID của người dùng", example = "a6c4d7a1-2d84-4fcb-9c49-4d5b2d1b9c7f")
    private final UUID userId;

    @Schema(description = "Vai trò của người dùng trong hệ thống", example = "owner")
    private final String role;

    public LoginResponse(
            String accessToken,
            String refreshToken,
            UUID sessionId,
            long expiresIn,
            UUID userId,
            String role) {

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.sessionId = sessionId;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}