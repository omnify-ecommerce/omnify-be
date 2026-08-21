package com.omnify.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),
    TOKEN_INVALID(HttpStatus.BAD_REQUEST, "Token xác thực không hợp lệ"),
    TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "Token xác thực đã hết hạn"),
    OTP_INVALID(HttpStatus.BAD_REQUEST, "Mã xác thực không đúng"),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không hợp lệ"),
    NEW_PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST, "Mật khẩu mới trùng với mật khẩu hiện tại"),
    CAPTCHA_FAILED(HttpStatus.BAD_REQUEST, "Xác thực captcha không thành công, vui lòng thử lại"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã được sử dụng"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Số điện thoại đã được sử dụng"),
    ACCOUNT_ALREADY_VERIFIED( HttpStatus.CONFLICT, "Tài khoản đã được xác thực"),
    ACCOUNT_PENDING_VERIFICATION(HttpStatus.CONFLICT, "Tài khoản đã được đăng ký nhưng chưa xác thực, vui lòng xác thực tài khoản"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy phiên đăng nhập hoặc đã đăng xuất"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Tài khoản đang bị tạm khóa"),
    ACCOUNT_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Tài khoản chưa được xác thực, vui lòng kiểm tra email/SMS"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Thông tin đăng nhập không chính xác"),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh token đã hết hạn hoặc đã bị thu hồi, vui lòng đăng nhập lại"),
    REFRESH_TOKEN_REUSE_DETECTED(HttpStatus.UNAUTHORIZED, "Phát hiện dấu hiệu bất thường, tất cả thiết bị đã được đăng xuất vì lý do bảo mật"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau"),
    RESEND_COOLDOWN( HttpStatus.TOO_MANY_REQUESTS, "Vui lòng đợi trước khi gửi lại email xác thực"),
    OTP_LOCKED(HttpStatus.TOO_MANY_REQUESTS, "Đã nhập sai quá số lần cho phép, vui lòng gửi lại mã mới"),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Bạn đã gửi quá nhiều yêu cầu, vui lòng thử lại sau");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}