package com.omnify.user.dto.request;

import com.omnify.common.constant.RegexPattern;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin yêu cầu thay đổi mật khẩu của người dùng")
public class ChangePasswordRequest {

    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    @Schema(
        description = "Mật khẩu hiện tại",
        example = "CurrentPassw0rd",
        requiredMode = Schema.RequiredMode.REQUIRED,
        type = "string",
        format = "password"
    )
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, max = 72, message = "Mật khẩu phải có từ 8 đến 72 ký tự")
    @Pattern(
        regexp = RegexPattern.PASSWORD,
        message = "Mật khẩu phải chứa ít nhất một chữ hoa, một chữ thường và một chữ số"
    )
    @Schema(
        description = "Mật khẩu mới",
        example = "NewPassw0rd",
        requiredMode = Schema.RequiredMode.REQUIRED,
        type = "string",
        format = "password"
    )
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu mới không được để trống")
    @Schema(
        description = "Xác nhận mật khẩu mới",
        example = "NewPassw0rd",
        requiredMode = Schema.RequiredMode.REQUIRED,
        type = "string",
        format = "password"
    )
    private String confirmPassword;
}
