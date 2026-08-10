package com.omnify.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Schema(
        name = "RegisterResponse",
        description = "Thông tin trả về sau khi đăng ký tài khoản thành công"
)
public class RegisterResponse {

    @Schema(description = "ID của người dùng vừa được tạo", example = "550e8400-e29b-41d4-a716-446655440000")
    private final UUID userId;

    @Schema(description = "Trạng thái của tài khoản sau khi đăng ký", example = "PENDING")
    private final String status;

    @Schema(description = "Kênh xác thực được sử dụng", example = "EMAIL_VERIFY")
    private final String verificationChannel;

    @Schema(description = "Vai trò được gán cho người dùng", example = "owner")
    private final String assignedRole;
}