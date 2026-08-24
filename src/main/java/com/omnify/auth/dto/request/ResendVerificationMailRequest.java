package com.omnify.auth.dto.request;

import com.omnify.common.constant.RegexPattern;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendVerificationMailRequest {

    @Schema(
        example = "user@example.com",
        description = "The email address used during registration"
    )
    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = RegexPattern.EMAIL, message = "Email không đúng định dạng")
    private String email;

}