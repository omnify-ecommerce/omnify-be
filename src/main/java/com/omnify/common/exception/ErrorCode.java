package com.omnify.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),
    TOKEN_INVALID(HttpStatus.BAD_REQUEST, "Token xác thực không hợp lệ"),
    TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "Token xác thực đã hết hạn"),
    OTP_INVALID(HttpStatus.BAD_REQUEST, "Mã xác thực không hợp lệ"),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không chính xác"),
    PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "Mật khẩu không hợp lệ"),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "Mật khẩu không khớp"),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "Mật khẩu mới trùng với mật khẩu hiện tại"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "Dữ liệu đã tồn tại, vui lòng kiểm tra lại"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy dữ liệu"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy phiên đăng nhập hoặc đã đăng xuất"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "Tài khoản đang bị tạm khóa"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "Tài khoản đang bị vô hiệu hóa"),
    ACCOUNT_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Tài khoản chưa được xác thực, vui lòng kiểm tra email/SMS"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Thông tin đăng nhập không chính xác"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau");

    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
