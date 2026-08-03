package com.omnify.auth.dto.request;

import com.omnify.common.constant.RegexPattern;
import com.omnify.common.validation.AtLeastOneContact;
import com.omnify.common.validation.EmailOrPhoneCarrier;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@AtLeastOneContact
public class RegisterRequest  implements EmailOrPhoneCarrier {

    @Schema(description = "Email đăng ký (bắt buộc nếu không có phone)", example = "owner@omnify.vn")
    @Pattern(regexp = RegexPattern.EMAIL, message = "Email không đúng định dạng")
    private String email;

    @Schema(description = "SĐT đăng ký định dạng VN (bắt buộc nếu không có email)", example = "0987654321")
    @Pattern(regexp = RegexPattern.PHONE_VN, message = "Số điện thoại không đúng định dạng")
    private String phone;

    @Schema(description = "Mật khẩu: tối thiểu 8 ký tự, có hoa/thường/số", example = "Passw0rd123")
    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = RegexPattern.PASSWORD,
            message = "Mật khẩu tối thiểu 8 ký tự, gồm chữ hoa, chữ thường và số")
    private String password;

    @Schema(description = "Họ tên người đăng ký (owner)", example = "Nguyễn Văn A")
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255, message = "Họ tên tối đa 255 ký tự")
    private String fullName;

    @Schema(description = "Tên công ty/shop — sẽ tạo tenant mới", example = "Omnify Demo Shop")
    @NotBlank(message = "Tên công ty/shop không được để trống")
    @Size(max = 255, message = "Tên công ty tối đa 255 ký tự")
    private String companyName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = (email == null || email.isBlank()) ? null : email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = (phone == null || phone.isBlank()) ? null : phone.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}