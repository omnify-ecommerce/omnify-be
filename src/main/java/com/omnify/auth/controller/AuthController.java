package com.omnify.auth.controller;

import com.omnify.auth.dto.request.*;
import com.omnify.auth.dto.response.LoginResponse;
import com.omnify.auth.dto.response.RegisterResponse;
import com.omnify.auth.service.AuthService;
import com.omnify.auth.service.SessionService;
import com.omnify.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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


@RestController
@RequestMapping("/api/v1/auth")
@Tag(
    name = "Authentication",
    description = "API for user registration, login, logout, email verification and token management"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Register a new account",
        description = "Creates a new user account and sends a verification code via email"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Registration successful. Please verify your account.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "message": "Đăng ký thành công, vui lòng xác thực tài khoản",
                      "data": {
                        "userId": "eee5a030-6b88-4f84-9a07-51004e3398de",
                        "status": "PENDING",
                        "verificationChannel": "EMAIL_VERIFICATION",
                        "assignedRole": "owner"
                      },
                      "errorCode": null,
                      "timestamp": "2026-08-14T02:22:29.583117700Z"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "VALIDATION_ERROR - Password must be at least 8 characters" +
                " and contain uppercase, lowercase, and numeric characters. " +
                "Email, phone number, first name, and last name must be valid and cannot be empty.",
            content = @Content),
        @ApiResponse(
            responseCode = "409",
            description = "EMAIL_ALREADY_EXISTS - The email address is already in use. <br>" +
                "PHONE_ALREADY_EXISTS - The phone number is already in use.",
            content = @Content),
        @ApiResponse(
            responseCode = "429",
            description = "RATE_LIMIT_EXCEEDED - Too many requests. Please try again later.",
            content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "INTERNAL_ERROR - An internal server error occurred. Please try again later.",
            content = @Content)

    })
    @PostMapping("/register")
    public ResponseEntity<com.omnify.common.response.ApiResponse<RegisterResponse>> register(
        @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(com.omnify.common.response.ApiResponse.success(response, "Đăng ký thành công, vui lòng xác thực tài khoản"));
    }

    //  login
    @Operation(
        summary = "Log in",
        description = "Authenticates the user and creates an access token and refresh token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                        {
                             "success": true,
                             "message": "Đăng nhập thành công",
                             "data": {
                                 "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjOTlhNzVkMy1mNGMzLTQ0ZjQtYjZiNC1mMTFkYzlmN2RjZmIiLCJyb2xlIjoib3duZXIiLCJpYXQiOjE3ODY2NzYzMjQsImV4cCI6MTc4NjY3NzIyNH0.gXWVeqqjS-0_GkKYSirRHy2tZpDLCWHLOjlsCxO7ZvE",
                                 "refreshToken": "JYwKTFdAb9rpC3uZSeB7Kj9k3AGVKw-MwfzOu0Eje00",
                                 "sessionId": "72749537-0d67-4aa6-b6d3-7899288a8ebb",
                                 "expiresIn": 900,
                                 "userId": "c99a75d3-f4c3-44f4-b6b4-f11dc9f7dcfb",
                                 "role": "owner"
                             },
                             "errorCode": null,
                             "timestamp": "2026-08-14T02:58:44.682107Z"
                        }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "VALIDATION_ERROR - Username/email/phone and password cannot be empty.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "INVALID_CREDENTIALS - The login credentials are incorrect.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "ACCOUNT_LOCKED - The account is temporarily locked.<br> " +
                "ACCOUNT_NOT_VERIFIED - The account has not been verified. Please check " +
                "your email",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "429",
            description = "RATE_LIMIT_EXCEEDED - Too many requests. Please try again later.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "INTERNAL_ERROR - An internal server error occured. Please try again later",
            content = @Content
        )
    })
    @PostMapping("/login")
    public ResponseEntity<com.omnify.common.response.ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(response, "Đăng nhập thành công"));
    }

    @Operation(
        summary = "Log out",
        description = "Logs the user out of the current session using the refresh token.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                        {
                            "success": true,
                            "message": "Đăng xuất thành công",
                            "data": null,
                            "errorCode": null,
                            "timestamp": "2026-08-14T03:16:58.385401Z"
                        }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "REFRESH_TOKEN_INVALID - The refresh token is missing, invalid, or expired.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "INTERNAL_ERROR - An internal server error occured. Please try again later",
            content = @Content
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> logout(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @Valid @RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken(), principal.userId());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Đăng xuất thành công"));
    }

    @Operation(
        summary = "Verify email",
        description = "Verify the user account using the email address and verification OTP"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Account verification successful.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                        {
                             "success": true,
                             "message": "Xác thực tài khoản thành công",
                             "data": null,
                             "errorCode": null,
                             "timestamp": "2026-08-14T03:21:38.025920Z"
                         }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "VALIDATION_ERROR - Email must be valid. Verification code must contain" +
                "exactly 6 digits. Email and verification code cannot be empty. <br>" +
                "TOKEN_EXPIRED - The verification token has expired.<br>" +
                "OTP_INVALID - The verification code is incorrect.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "USER_NOT_FOUND - The user could not be found.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "ACCOUNT_ALREADY_VERIFIED - The account has already been verified.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "429",
            description = "RATE_LIMIT_EXCEEDED - Too many requests. Please try again later.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "INTERNAL_ERROR - An internal server error occured. Please try again later",
            content = @Content
        )
    })
    @PostMapping("/verify-email")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> verifyEmail(
        @Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Xác thực tài khoản thành công"));
    }

    @Operation(
        summary = "Resend verification code",
        description = "Resend the email verification OTP to the user email address."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Verification email has been resent.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                        {
                             "success": true,
                             "message": "Email xác thực đã được gửi lại",
                             "data": null,
                             "errorCode": null,
                             "timestamp": "2026-08-14T03:21:38.025920Z"
                         }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "VALIDATION_ERROR - Email cannot be empty and must have a valid format",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "USER_NOT_FOUND - The user could not be found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "ACCOUNT_ALREADY_VERIFIED - The account has already been verified.<br>" +
                "RESEND_COOLDOWN - Please wait before requesting another verification email.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "429",
            description = "RATE_LIMIT_EXCEEDED - Too many requests. Please try again later.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "INTERNAL_ERROR - An internal server error occured. Please try again later",
            content = @Content
        )
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> resendVerification(
        @Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Email xác thực đã được gửi lại"));
    }

    @Operation(
        summary = "Refresh access token",
        description = "Uses a valid refresh token to issue a new access token."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "token refresh successful.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                        {
                              "success": true,
                              "message": "Làm mới token thành công",
                              "data": {
                                  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkY2ZiNDcwNi1kMTE0LTQ4YjItOTU5NS04MDM5ZGQxMThlOTUiLCJyb2xlIjoib3duZXIiLCJpYXQiOjE3ODY2NzgwMjMsImV4cCI6MTc4NjY3ODkyM30.vrPImUH22TgeeFDjxcKU3k6zSwlvyXVzhKCynsUrzWc",
                                  "refreshToken": "e2TY7c7SLefyuvUgzQqM0G1O6IUF72JhcsADsBPLpK4",
                                  "sessionId": "a6dd558a-389d-44ed-a8d1-c95206d2330b",
                                  "expiresIn": 900,
                                  "userId": "dcfb4706-d114-48b2-9595-8039dd118e95",
                                  "role": "owner"
                              },
                              "errorCode": null,
                              "timestamp": "2026-08-14T03:27:03.230033800Z"
                          }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "VALIDATION_ERROR - Refresh token cannot be empty.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "REFRESH_TOKEN_INVALID - The refresh token has expired or has been revoked" +
                "Please log in again <br>" +
                "REFRESH_TOKEN_EXPIRED - The refresh token has expired or has been revoked. Please log in again.<br>" +
                "REFRESH_TOKEN_REUSE_DETECTED - Suspicious activity detected. All sessions have been" +
                "revoked for security reason",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "ACCOUNT_NOT_VERIFIED - The account has not been verified. Please check your email"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "USER_NOT_FOUND - The user could not be found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "429",
            description = "RATE_LIMIT_EXCEEDED - Too many requests. Please try again later.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "INTERNAL_ERROR - An internal server error occured. Please try again later",
            content = @Content
        )
    })
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