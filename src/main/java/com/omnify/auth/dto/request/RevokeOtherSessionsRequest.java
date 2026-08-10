package com.omnify.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Schema(
        name = "RevokeOtherSessionsRequest",
        description = "Yêu cầu đăng xuất tất cả các phiên đăng nhập khác, ngoại trừ phiên hiện tại"
)
@Getter
@Setter
public class RevokeOtherSessionsRequest {

    @NotNull(message = "currentSessionId không được để trống")
    @Schema(
            description = "ID của phiên đăng nhập hiện tại sẽ được giữ lại. Tất cả các phiên khác sẽ bị thu hồi.",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID currentSessionId;
}