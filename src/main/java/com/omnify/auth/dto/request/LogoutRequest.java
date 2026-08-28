package com.omnify.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(
    name = "LogoutRequest",
    description = "Request to log out of the current session"
)
@Getter
@Setter
public class LogoutRequest {

    @NotNull(message = "refresh token không được để trống")
    @Schema(
        description = "Refresh token of the current session to log out",
        example = "D4DaQHdH79qfbnoP8Lwf06ZNIINi7fYU-xE9YSu5gnc",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;

}