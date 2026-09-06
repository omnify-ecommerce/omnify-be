package com.omnify.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(
    name = "RegisterResponse",
    description = "Information returned after a successful account registration"
)
public class RegisterResponse {

    @Schema(description = "ID of the newly created user", example = "eee5a030-6b88-4f84-9a07-51004e3398de")
    private final UUID userId;

    @Schema(description = "Account status after registration", example = "PENDING")
    private final String status;

    @Schema(description = "Verification channel used", example = "EMAIL_VERIFICATION")
    private final String verificationChannel;

    @Schema(description = "Role assigned to the user", example = "owner")
    private final String assignedRole;
}