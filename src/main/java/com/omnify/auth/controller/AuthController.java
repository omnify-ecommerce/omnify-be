package com.omnify.auth.controller;

import com.omnify.auth.dto.request.*;
import com.omnify.auth.dto.response.LoginResponse;
import com.omnify.auth.dto.response.RegisterResponse;
import com.omnify.auth.service.AuthService;
import com.omnify.auth.service.SessionService;
import com.omnify.common.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;

    public AuthController(AuthService authService, SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    // dang ky
    @PostMapping("/register")
    public ResponseEntity<com.omnify.common.response.ApiResponse<RegisterResponse>> register(
        @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(com.omnify.common.response.ApiResponse.success(response, "Đăng ký thành công, vui lòng xác thực tài khoản"));
    }

    //  login
    @PostMapping("/login")
    public ResponseEntity<com.omnify.common.response.ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(response, "Đăng nhập thành công"));
    }

    // dang xuat
    @PostMapping("/logout")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> logout(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @Valid @RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken(), principal.userId());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Đăng xuất thành công"));
    }

    // xac thu email
    @PostMapping("/verify-email")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> verifyEmail(
        @Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Xác thực tài khoản thành công"));
    }

    // gui lai ma otp email
    @PostMapping("/resend-verification")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> resendVerification(
        @Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Email xác thực đã được gửi lại"));
    }

    // cap access token moi
    @PostMapping("/refresh")
    public ResponseEntity<com.omnify.common.response.ApiResponse<LoginResponse>> refresh(
        @Valid @RequestBody RefreshTokenRequest request,
        HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.refreshToken(request.getRefreshToken(), ipAddress, userAgent);
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(response, "Làm mới token thành công"));
    }
}