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
        example = "a6dd558a-389d-44ed-a8d1-c95206d2330b",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID currentSessionId;
}