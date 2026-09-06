package com.omnify.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Schema(
    name = "RevokeOtherSessionsRequest",
    description = "Request to log out of all other sessions except the current one"
)
@Getter
@Setter
public class RevokeOtherSessionsRequest {

    @NotNull(message = "currentSessionId không được để trống")
    @Schema(
        description = "ID of the current session to keep. All other sessions will be revoked.",
        example = "a6dd558a-389d-44ed-a8d1-c95206d2330b",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID currentSessionId;
}