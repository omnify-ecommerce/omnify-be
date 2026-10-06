package com.omnify.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.omnify.auth.dto.request.ForgotPasswordRequest;
import com.omnify.auth.dto.request.ResetPasswordRequest;
import com.omnify.auth.service.PasswordResetService;
import com.omnify.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor 
@Tag(name = "Password Reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    
    @Operation(
        summary = "Forgot password",
        description = "Send a password reset link via email if the account exists"
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse
            .success(null, "Nếu tài khoản tồn tại, một liên kết đặt lại mật khẩu đã được gửi đi"));
    }

    @Operation(
        summary = "Reset password",
        description = "Reset password using the verification token from the reset link"
    )
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Mật khẩu đã được đặt lại thành công"));
    }
}
