package com.omnify.auth.dto.request;

import com.omnify.common.constant.RegexPattern;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyEmailRequest {

    @Schema(
        example = "user@example.com",
        description = "The email address used during registration"
    )
    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = RegexPattern.EMAIL, message = "Email không đúng định dạng")
    private String email;

    @Schema(
        example = "123456",
        description = "The 6-digit verification code sent to the user's email"
    )
    @NotBlank(message = "Mã xác thực không được để trống")
    @Pattern(regexp = "\\d{6}", message = "Mã xác thực phải gồm 6 chữ số")
    private String otpCode;
}