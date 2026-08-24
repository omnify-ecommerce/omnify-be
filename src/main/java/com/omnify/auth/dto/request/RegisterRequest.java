package com.omnify.auth.dto.request;

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
public class RegisterRequest implements EmailOrPhoneCarrier {

    @Schema(description = "Email đăng ký", example = "user123@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Pattern(regexp = RegexPattern.EMAIL, message = "Email không đúng định dạng")
    private String email;

    @Schema(description = "SĐT đăng ký định dạng VN (không bắt buộc)", example = "0987654321", nullable = true)
    @Pattern(regexp = RegexPattern.PHONE_VN, message = "Số điện thoại không đúng định dạng")
    private String phone;

    @Schema(description = "Mật khẩu: tối thiểu 8 ký tự, có hoa/thường/số", example = "Passw0rd123")
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 72, message = "Mật khẩu phải có độ dài từ 8 đến 72 ký tự")
    @Pattern(regexp = RegexPattern.PASSWORD,
        message = "Mật khẩu tối thiểu 8 ký tự, gồm chữ hoa, chữ thường và số")
    private String password;

    @Schema(description = "Tên riêng của user", example = "Tu")
    @NotBlank(message = "Tên riêng không được để trống")
    @Size(max = 100, message = "Tên riêng tối đa 100 kí tự")
    private String firstName;

    @Schema(description = "Họ tên của user", example = "Nguyen")
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 kí tự")
    private String lastName;

    @Schema(description = "Token captcha (reCAPTCHA v3) sinh ra ở FE", example = "03AGdBq27...")
    private String captchaToken;

    public void setEmail(String email) {
        this.email = (email == null || email.isBlank()) ? null : email.trim();
    }

    public void setPhone(String phone) {
        this.phone = (phone == null || phone.isBlank()) ? null : phone.trim();
    }


}