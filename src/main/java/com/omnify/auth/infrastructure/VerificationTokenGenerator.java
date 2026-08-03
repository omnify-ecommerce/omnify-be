package com.omnify.auth.infrastructure;

import com.omnify.common.security.TokenHasher;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class VerificationTokenGenerator {

    private final SecureRandom secureRandom = new SecureRandom();
    private final TokenHasher tokenHasher;

    public VerificationTokenGenerator(TokenHasher tokenHasher) {
        this.tokenHasher = tokenHasher;
    }

    public String generateEmailToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String generatePhoneOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    public String hash(String rawValue) {
        return tokenHasher.hash(rawValue);
    }
}