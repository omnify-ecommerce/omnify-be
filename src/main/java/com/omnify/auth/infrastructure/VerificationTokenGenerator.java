package com.omnify.auth.infrastructure;

import com.omnify.common.security.TokenHasher;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class VerificationTokenGenerator {

    private final SecureRandom secureRandom = new SecureRandom();
    private final TokenHasher tokenHasher;

    public VerificationTokenGenerator(TokenHasher tokenHasher) {
        this.tokenHasher = tokenHasher;
    }

    /** Sinh mã OTP 6 số dùng cho email verify (đổi từ token 256-bit sang OTP, tránh Brevo link-tracking). */
    public String generateEmailToken() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    public String generatePhoneOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    public String hash(String rawValue) {
        return tokenHasher.hash(rawValue);
    }
}