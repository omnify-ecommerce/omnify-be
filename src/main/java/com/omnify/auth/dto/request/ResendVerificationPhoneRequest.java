package com.omnify.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendVerificationPhoneRequest {
    @Schema(example = "0912345678", description = "Số điện thoại dùng khi đăng ký")
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;
}
