package com.omnify.auth.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPhoneRequest {
    @Schema(example = "0794264041" ,description = "SDT dung khi dang ky")
    @NotBlank( message = "So dien thoai khong duoc de trong")
    private String phone;
    @Schema(example = "123456", description = "Ma OTP 6 so gui toi SDT")
    @NotBlank (message = "Ma xac thuc khong duoc de trong")
    @Pattern(regexp = "\\d{6}", message = "Ma xac thuc phai gom 6 chu so")
    private String otpCode;
}
