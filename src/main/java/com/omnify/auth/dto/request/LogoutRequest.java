package com.omnify.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(
        name = "LogoutRequest",
        description = "Yêu cầu đăng xuất một phiên đăng nhập cụ thể"
)
public class LogoutRequest {

    @NotNull(message = "sessionId không được để trống")
    @Schema(
            description = "ID của phiên đăng nhập cần đăng xuất",
            example = "7314e147-1301-4e61-8988-3a543cad9608",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID sessionId;

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }
}