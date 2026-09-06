package com.omnify.auth.dto.request;

import com.omnify.auth.infrastructure.CaptchaCarrier;
import com.omnify.common.validation.AtLeastOneContact;
import com.omnify.common.validation.EmailOrPhoneCarrier;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@AtLeastOneContact
@Schema(
    name = "LoginRequest",
    description = "Login information using email or phone number"
)
@Getter
@Setter
public class LoginRequest implements EmailOrPhoneCarrier, CaptchaCarrier {

    @Schema(
        description = "Login email. Required if phone number is not provided.",
        example = "user@example.com",
        nullable = true
    )
    private String email;

    @Schema(
        description = "Login phone number. Required if email is not provided.",
        example = "0912345678",
        nullable = true
    )
    private String phone;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Schema(
        description = "Account password",
        example = "P@ssw0rd123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @Schema(
        description = "Captcha token (reCAPTCHA v3), required after exceeding the allowed number of failed login attempts",
        example = "03AGdBq27...",
        nullable = true
    )
    private String captchaTokenV3;

    @Schema(
        description = "Captcha token (reCAPTCHA v2). Send this alone (captchaTokenV3 not required) after " +
            "the response returns CAPTCHA_STEP_UP_REQUIRED (v3 score too low)",
        example = "03AGdBq27...",
        nullable = true
    )
    private String captchaTokenV2;

    public void setEmail(String email) {
        this.email = (email == null || email.isBlank()) ? null : email.trim();
    }

    public void setPhone(String phone) {
        this.phone = (phone == null || phone.isBlank()) ? null : phone.trim();
    }

}