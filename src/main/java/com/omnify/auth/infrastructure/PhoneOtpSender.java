package com.omnify.auth.infrastructure;

public interface PhoneOtpSender {
    void sendOtp(String phone, String fullName, String otpCode);
}
