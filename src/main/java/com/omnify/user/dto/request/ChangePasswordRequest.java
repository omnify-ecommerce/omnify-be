package com.omnify.user.dto.request;

import com.omnify.common.constant.RegexPattern;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to change the current user's password")
public record ChangePasswordRequest(
    @Schema(
        description = "The current password",
        example = "OldPassword123",
        format = "password"
    )
    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    String currentPassword,

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
    String newPassword,

    @Schema(
        description = "Confirmation of the new password",
        example = "NewPassword123",
        format = "password"
    )
    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    String confirmPassword
) {}
