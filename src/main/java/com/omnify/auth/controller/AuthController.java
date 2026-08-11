package com.omnify.auth.controller;

import com.omnify.auth.dto.request.*;
import com.omnify.auth.dto.response.LoginResponse;
import com.omnify.auth.dto.response.RegisterResponse;
import com.omnify.auth.service.AuthService;
import com.omnify.auth.service.SessionService;
import com.omnify.common.security.AuthenticatedUser;
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
import org.springframework.http.MediaType;
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

    // dang ky
    @Operation(
            summary = "Đăng ký tài khoản mới",
            description = "Tạo user mới với role là owner. Cần email hoặc phone (hiện tại bắt buộc cần email). "
                    + "Rate limit: 3 request/phút/IP."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Đăng ký thành công",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(name = "VALIDATION_ERROR",
                                            value = "{\"success\":false,\"message\":\"Mật khẩu tối thiểu 8 ký tự, gồm chữ hoa, chữ thường và số\",\"errorCode\":\"VALIDATION_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"),
                                    @ExampleObject(name = "INVALID_CONTACT",
                                            value = "{\"success\":false,\"message\":\"Phải cung cấp email hoặc số điện thoại\",\"errorCode\":\"INVALID_CONTACT\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")
                            })),
            @ApiResponse(responseCode = "409", description = "Email hoặc số điện thoại đã được sử dụng",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "DUPLICATE_RESOURCE",
                                    value = "{\"success\":false,\"message\":\"Dữ liệu đã tồn tại, vui lòng kiểm tra lại\",\"errorCode\":\"DUPLICATE_RESOURCE\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "429", description = "Vượt quá giới hạn số lần đăng ký cho phép",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "RATE_LIMIT_EXCEEDED",
                                    value = "{\"success\":false,\"message\":\"Bạn đã gửi quá nhiều yêu cầu, vui lòng thử lại sau\",\"errorCode\":\"RATE_LIMIT_EXCEEDED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "INTERNAL_ERROR",
                                    value = "{\"success\":false,\"message\":\"Lỗi hệ thống, vui lòng thử lại sau\",\"errorCode\":\"INTERNAL_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")))
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
            summary = "Đăng nhập",
            description = "Trả về access token (JWT, TTL ngắn) + refresh token (dài hạn, theo thiết bị). "
                    + "Tài khoản chưa xác thực (PENDING) sẽ bị từ chối. Rate limit: 5 request/phút/IP."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(name = "VALIDATION_ERROR",
                                            value = "{\"success\":false,\"message\":\"Mật khẩu không được để trống\",\"errorCode\":\"VALIDATION_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"),
                                    @ExampleObject(name = "INVALID_CONTACT",
                                            value = "{\"success\":false,\"message\":\"Phải cung cấp email hoặc số điện thoại\",\"errorCode\":\"INVALID_CONTACT\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")
                            })),
            @ApiResponse(responseCode = "401", description = "Sai email/phone hoặc mật khẩu",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "INVALID_CREDENTIALS",
                                    value = "{\"success\":false,\"message\":\"Thông tin đăng nhập không chính xác\",\"errorCode\":\"INVALID_CREDENTIALS\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "403", description = "Tài khoản bị khóa hoặc chưa xác thực",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(name = "ACCOUNT_LOCKED",
                                            value = "{\"success\":false,\"message\":\"Tài khoản đang bị tạm khóa\",\"errorCode\":\"ACCOUNT_LOCKED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"),
                                    @ExampleObject(name = "ACCOUNT_NOT_VERIFIED",
                                            value = "{\"success\":false,\"message\":\"Tài khoản chưa được xác thực, vui lòng kiểm tra email/SMS\",\"errorCode\":\"ACCOUNT_NOT_VERIFIED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")
                            })),
            @ApiResponse(responseCode = "429", description = "Vượt quá giới hạn số lần đăng nhập cho phép",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "RATE_LIMIT_EXCEEDED",
                                    value = "{\"success\":false,\"message\":\"Bạn đã gửi quá nhiều yêu cầu, vui lòng thử lại sau\",\"errorCode\":\"RATE_LIMIT_EXCEEDED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "INTERNAL_ERROR",
                                    value = "{\"success\":false,\"message\":\"Lỗi hệ thống, vui lòng thử lại sau\",\"errorCode\":\"INTERNAL_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")))
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

    // dang xuat
    @Operation(
            summary = "Đăng xuất phiên hiện tại",
            description = "Yêu cầu Bearer token hợp lệ. Thu hồi refresh token của session được chỉ định."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "VALIDATION_ERROR",
                                    value = "{\"success\":false,\"message\":\"sessionId không được để trống\",\"errorCode\":\"VALIDATION_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "401", description = "Thiếu token hoặc token không hợp lệ/hết hạn "
                    + "(xử lý ở JwtAuthenticationFilter/SecurityConfig, không đi qua GlobalExceptionHandler)"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phiên đăng nhập hoặc phiên không thuộc về user hiện tại",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "SESSION_NOT_FOUND",
                                    value = "{\"success\":false,\"message\":\"Không tìm thấy phiên đăng nhập hoặc đã đăng xuất\",\"errorCode\":\"SESSION_NOT_FOUND\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "INTERNAL_ERROR",
                                    value = "{\"success\":false,\"message\":\"Lỗi hệ thống, vui lòng thử lại sau\",\"errorCode\":\"INTERNAL_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")))
    })
    @PostMapping("/logout")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> logout(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody LogoutRequest request) {
        sessionService.revokeSession(request.getSessionId(), principal.userId());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Đăng xuất thành công"));
    }

    // xac thu email
    @Operation(
            summary = "Xác thực email",
            description = "Nhận mã otp và thực hiện xác thực tài khoản từ PENDING thành ACTIVE. Rate limit: 5 request/phút/IP."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Xác thực tài khoản thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu/OTP không hợp lệ hoặc token hết hạn",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(name = "VALIDATION_ERROR",
                                            value = "{\"success\":false,\"message\":\"Mã xác thực phải gồm 6 chữ số\",\"errorCode\":\"VALIDATION_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"),
                                    @ExampleObject(name = "TOKEN_INVALID",
                                            value = "{\"success\":false,\"message\":\"Token xác thực không hợp lệ\",\"errorCode\":\"TOKEN_INVALID\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"),
                                    @ExampleObject(name = "TOKEN_EXPIRED",
                                            value = "{\"success\":false,\"message\":\"Token xác thực đã hết hạn\",\"errorCode\":\"TOKEN_EXPIRED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"),
                                    @ExampleObject(name = "OTP_INVALID",
                                            value = "{\"success\":false,\"message\":\"Mã xác thực không đúng\",\"errorCode\":\"OTP_INVALID\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")
                            })),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "USER_NOT_FOUND",
                                    value = "{\"success\":false,\"message\":\"Không tìm thấy người dùng\",\"errorCode\":\"USER_NOT_FOUND\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "409", description = "Tài khoản đã được xác thực trước đó",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "ACCOUNT_ALREADY_VERIFIED",
                                    value = "{\"success\":false,\"message\":\"Tài khoản đã được xác thực\",\"errorCode\":\"ACCOUNT_ALREADY_VERIFIED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "429", description = "Nhập sai OTP quá số lần cho phép hoặc vượt rate limit",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(name = "OTP_LOCKED",
                                            value = "{\"success\":false,\"message\":\"Đã nhập sai quá số lần cho phép, vui lòng gửi lại mã mới\",\"errorCode\":\"OTP_LOCKED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"),
                                    @ExampleObject(name = "RATE_LIMIT_EXCEEDED",
                                            value = "{\"success\":false,\"message\":\"Bạn đã gửi quá nhiều yêu cầu, vui lòng thử lại sau\",\"errorCode\":\"RATE_LIMIT_EXCEEDED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")
                            })),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "INTERNAL_ERROR",
                                    value = "{\"success\":false,\"message\":\"Lỗi hệ thống, vui lòng thử lại sau\",\"errorCode\":\"INTERNAL_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")))
    })
    @PostMapping("/verify-email")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Xác thực tài khoản thành công"));
    }

    // gui lai ma otp email
    @Operation(
            summary = "Gửi lại mã xác thực tài khoản",
            description = "Gửi lại mã otp qua email của người dùng để xác thực lại. Cooldown 60s giữa 2 lần gửi. Rate limit: 3 request/phút/IP."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email xác thực đã được gửi lại"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "VALIDATION_ERROR",
                                    value = "{\"success\":false,\"message\":\"Email không đúng định dạng\",\"errorCode\":\"VALIDATION_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "USER_NOT_FOUND",
                                    value = "{\"success\":false,\"message\":\"Không tìm thấy người dùng\",\"errorCode\":\"USER_NOT_FOUND\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "409", description = "Tài khoản đã được xác thực trước đó",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "ACCOUNT_ALREADY_VERIFIED",
                                    value = "{\"success\":false,\"message\":\"Tài khoản đã được xác thực\",\"errorCode\":\"ACCOUNT_ALREADY_VERIFIED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "429", description = "Gửi lại quá sớm (cooldown) hoặc vượt rate limit",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(name = "RESEND_COOLDOWN",
                                            value = "{\"success\":false,\"message\":\"Vui lòng đợi trước khi gửi lại email xác thực\",\"errorCode\":\"RESEND_COOLDOWN\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"),
                                    @ExampleObject(name = "RATE_LIMIT_EXCEEDED",
                                            value = "{\"success\":false,\"message\":\"Bạn đã gửi quá nhiều yêu cầu, vui lòng thử lại sau\",\"errorCode\":\"RATE_LIMIT_EXCEEDED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")
                            })),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "INTERNAL_ERROR",
                                    value = "{\"success\":false,\"message\":\"Lỗi hệ thống, vui lòng thử lại sau\",\"errorCode\":\"INTERNAL_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")))
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Email xác thực đã được gửi lại"));
    }

    // cap access token moi
    @Operation(
            summary = "Làm mới access token",
            description = "Dùng refresh token để lấy access token mới mà không cần đăng nhập lại. "
                    + "Refresh token cũ sẽ bị thu hồi ngay (rotation), trả về cặp token mới. Rate limit: 10 request/phút/IP."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Làm mới token thành công",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "VALIDATION_ERROR",
                                    value = "{\"success\":false,\"message\":\"refreshToken không được để trống\",\"errorCode\":\"VALIDATION_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "401", description = "Refresh token không hợp lệ/hết hạn/bị tái sử dụng",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(name = "REFRESH_TOKEN_INVALID",
                                            value = "{\"success\":false,\"message\":\"Refresh token không hợp lệ\",\"errorCode\":\"REFRESH_TOKEN_INVALID\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"),
                                    @ExampleObject(name = "REFRESH_TOKEN_EXPIRED",
                                            value = "{\"success\":false,\"message\":\"Refresh token đã hết hạn hoặc đã bị thu hồi, vui lòng đăng nhập lại\",\"errorCode\":\"REFRESH_TOKEN_EXPIRED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"),
                                    @ExampleObject(name = "REFRESH_TOKEN_REUSE_DETECTED",
                                            value = "{\"success\":false,\"message\":\"Phát hiện dấu hiệu bất thường, tất cả thiết bị đã được đăng xuất vì lý do bảo mật\",\"errorCode\":\"REFRESH_TOKEN_REUSE_DETECTED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")
                            })),
            @ApiResponse(responseCode = "429", description = "Vượt quá giới hạn số lần refresh cho phép",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "RATE_LIMIT_EXCEEDED",
                                    value = "{\"success\":false,\"message\":\"Bạn đã gửi quá nhiều yêu cầu, vui lòng thử lại sau\",\"errorCode\":\"RATE_LIMIT_EXCEEDED\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "INTERNAL_ERROR",
                                    value = "{\"success\":false,\"message\":\"Lỗi hệ thống, vui lòng thử lại sau\",\"errorCode\":\"INTERNAL_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")))
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