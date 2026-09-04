package com.omnify.auth.dto.request;

import com.omnify.auth.infrastructure.CaptchaCarrier;
import com.omnify.common.constant.RegexPattern;
import com.omnify.common.validation.AtLeastOneContact;
import com.omnify.common.validation.EmailOrPhoneCarrier;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AtLeastOneContact
public class RegisterRequest implements EmailOrPhoneCarrier, CaptchaCarrier {

    @Schema(description = "Registration email", example = "user123@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Pattern(regexp = RegexPattern.EMAIL, message = "Email không đúng định dạng")
    private String email;

    @Schema(description = "Registration phone number in VN format (optional)", example = "0987654321", nullable = true)
    @Pattern(regexp = RegexPattern.PHONE_VN, message = "Số điện thoại không đúng định dạng")
    private String phone;

    @Schema(description = "Password: at least 8 characters, with uppercase, lowercase and numeric characters", example = "Passw0rd123")
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 72, message = "Mật khẩu phải có độ dài từ 8 đến 72 ký tự")
    @Pattern(regexp = RegexPattern.PASSWORD,
        message = "Mật khẩu tối thiểu 8 ký tự, gồm chữ hoa, chữ thường và số")
    private String password;

    @Schema(description = "User's first name", example = "Tu")
    @NotBlank(message = "Tên riêng không được để trống")
    @Size(max = 100, message = "Tên riêng tối đa 100 kí tự")
    private String firstName;

    @Schema(description = "User's last name", example = "Nguyen")
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 kí tự")
    private String lastName;

    @Schema(description = "Captcha token (reCAPTCHA v3) generated on the FE", example = "03AGdBq27...")
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