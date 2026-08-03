package com.omnify.common.constant;

public final class RegexPattern {

    private RegexPattern() {
    }

    public static final String EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    //  0xxxxxxxxx và +84xxxxxxxxx
    public static final String PHONE_VN = "^(\\+84|0)(3|5|7|8|9)[0-9]{8}$";

    // Tối thiểu 8 ký tự, có chữ hoa, chữ thường, số
    public static final String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$";
}