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

    @Schema(description = "ID của người dùng vừa được tạo", example = "eee5a030-6b88-4f84-9a07-51004e3398de")
    private final UUID userId;

    @Schema(description = "Trạng thái của tài khoản sau khi đăng ký", example = "PENDING")
    private final String status;

    @Schema(description = "Kênh xác thực được sử dụng", example = "EMAIL_VERIFICATION")
    private final String verificationChannel;

    @Schema(description = "Vai trò được gán cho người dùng", example = "owner")
    private final String assignedRole;
}