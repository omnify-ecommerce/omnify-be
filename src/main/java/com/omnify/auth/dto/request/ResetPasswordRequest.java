package com.omnify.auth.dto.request;

import com.omnify.common.constant.RegexPattern;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to reset the user's password")
public record ResetPasswordRequest(
    @Schema(description = "The password reset token")
    @NotBlank(message = "Token không được để trống")
    String token,

    @Schema(
        description = "The new password",
        example = "NewPassword123",
        format = "password"
    )
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, max = 72, message = "Mật khẩu phải có độ dài từ 8 đến 72 ký tự")
    @Pattern(
        regexp = RegexPattern.PASSWORD,
        message = "Mật khẩu phải chứa ít nhất một chữ hoa, một chữ thường và một chữ số"
    )
    String newPassword
) {}
