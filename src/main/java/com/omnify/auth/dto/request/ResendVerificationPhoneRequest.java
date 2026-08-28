package com.omnify.auth.dto.request;

import com.omnify.common.constant.RegexPattern;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendVerificationPhoneRequest {
    @Schema(example = "0912345678", description = "Phone number used during registration")
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = RegexPattern.PHONE_VN, message = "Số điện thoại không đúng định dạng")
    private String phone;
}
