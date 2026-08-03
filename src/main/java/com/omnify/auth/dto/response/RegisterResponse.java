package com.omnify.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(
        name = "RegisterResponse",
        description = "Thông tin trả về sau khi đăng ký tài khoản thành công"
)
public class RegisterResponse {

    @Schema(
            description = "ID của người dùng vừa được tạo",
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private final UUID userId;

    @Schema(
            description = "ID của công ty mà người dùng thuộc về (null nếu không áp dụng)",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            nullable = true
    )
    private final UUID companyId;

    @Schema(
            description = "Trạng thái của tài khoản sau khi đăng ký",
            example = "PENDING_VERIFICATION"
    )
    private final String status;

    @Schema(
            description = "Kênh xác thực được sử dụng",
            example = "EMAIL"
    )
    private final String verificationChannel;

    @Schema(
            description = "Vai trò được gán cho người dùng",
            example = "OWNER"
    )
    private final String assignedRole;

    public RegisterResponse(
            UUID userId,
            UUID companyId,
            String status,
            String verificationChannel,
            String assignedRole) {

        this.userId = userId;
        this.companyId = companyId;
        this.status = status;
        this.verificationChannel = verificationChannel;
        this.assignedRole = assignedRole;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getStatus() {
        return status;
    }

    public String getVerificationChannel() {
        return verificationChannel;
    }

    public String getAssignedRole() {
        return assignedRole;
    }
}