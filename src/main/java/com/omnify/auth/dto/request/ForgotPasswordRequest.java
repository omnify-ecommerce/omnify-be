package com.omnify.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to send password reset email")
public record ForgotPasswordRequest(
    @NotBlank(message = "Địa chỉ email không được để trống")
    @Email(message = "Địa chỉ email phải hợp lệ")
    String email
) {}
