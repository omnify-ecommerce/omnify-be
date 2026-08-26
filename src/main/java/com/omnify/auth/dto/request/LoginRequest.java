package com.omnify.auth.dto.request;

import com.omnify.common.validation.AtLeastOneContact;
import com.omnify.common.validation.EmailOrPhoneCarrier;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@AtLeastOneContact
@Schema(
    name = "LoginRequest",
    description = "Thông tin đăng nhập bằng email hoặc số điện thoại"
)
@Getter
@Setter
public class LoginRequest implements EmailOrPhoneCarrier {

    @Schema(
        description = "Email đăng nhập. Bắt buộc nếu không truyền số điện thoại.",
        example = "user@example.com",
        nullable = true
    )
    private String email;

    @Schema(
        description = "Số điện thoại đăng nhập. Bắt buộc nếu không truyền email.",
        example = "0912345678",
        nullable = true
    )
    private String phone;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Schema(
        description = "Mật khẩu tài khoản",
        example = "P@ssw0rd123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @Schema(
        description = "Token captcha (reCAPTCHA v3), bat buoc sau khi dang nhap sai qua so lan cho phep",
        example = "03AGdBq27...",
        nullable = true
    )
    private String captchaToken;

    public void setEmail(String email) {
        this.email = (email == null || email.isBlank()) ? null : email.trim();
    }

    public void setPhone(String phone) {
        this.phone = (phone == null || phone.isBlank()) ? null : phone.trim();
    }

}