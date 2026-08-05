package com.omnify.auth.controller;

import com.omnify.auth.dto.request.*;
import com.omnify.auth.dto.response.LoginResponse;
import com.omnify.auth.dto.response.RegisterResponse;
import com.omnify.auth.service.AuthService;
import com.omnify.auth.service.SessionService;
import com.omnify.common.response.ApiResponse;
import com.omnify.common.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Đăng ký, đăng nhập, đăng xuất")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;

    public AuthController(AuthService authService, SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    @Operation(summary = "Đăng ký tài khoản mới",
            description = "Tạo company mới + user owner của company đó. Cần email HOẶC phone (ít nhất 1 trong 2).")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Đăng ký thành công, vui lòng xác thực tài khoản"));
    }

    @Operation(summary = "Đăng nhập",
            description = "Trả về access token (JWT, TTL ngắn) + refresh token (dài hạn, theo thiết bị). "
                    + "Tài khoản chưa xác thực (PENDING) sẽ bị từ chối.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request,
                                                            HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập thành công"));
    }

    @Operation(summary = "Đăng xuất phiên hiện tại",
            description = "Yêu cầu Bearer token hợp lệ. Thu hồi refresh token của session được chỉ định.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody LogoutRequest request) {
        sessionService.revokeSession(request.getSessionId(), principal.userId());
        return ResponseEntity.ok(ApiResponse.success(null, "Đăng xuất thành công"));
    }
    @Operation(summary = "Xác thực email",
            description = "Nhận mã otp và thực hiện xác thực tài khoản từ PENDING thành ACTIVE.")
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(ApiResponse.success(null, "Xác thực tài khoản thành công"));
    }
    @Operation(summary = "Gửi lại mã xác thực tài khoản",
            description = "Gửi lại mã otp qua email của người dùng để xác thực lại")
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null, "Email xác thực đã được gửi lại"));
    }

    
    @Operation(summary = "Làm mới access token",
            description = "Dùng refresh token để lấy access token mới mà không cần đăng nhập lại. "
                    + "Refresh token cũ sẽ bị thu hồi ngay (rotation), trả về cặp token mới.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.refreshToken(request.getRefreshToken(), ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success(response, "Làm mới token thành công"));
    }
}