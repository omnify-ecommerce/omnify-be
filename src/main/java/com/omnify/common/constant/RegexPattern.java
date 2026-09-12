package com.omnify.common.constant;

public final class RegexPattern {

    public static final String EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    // 0xxxxxxxxx và +84xxxxxxxxx
    public static final String PHONE_VN = "^(\\+84|0)(3|5|7|8|9)[0-9]{8}$";

    // Có ít nhất một chữ hoa, một chữ thường và một chữ số
    public static final String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$";

    private RegexPattern() {
    }
}
