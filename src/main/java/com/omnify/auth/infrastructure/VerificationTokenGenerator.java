package com.omnify.auth.infrastructure;

import com.omnify.security.TokenHasher;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class VerificationTokenGenerator {

    private final SecureRandom secureRandom = new SecureRandom();
    private final TokenHasher tokenHasher;

    public VerificationTokenGenerator(TokenHasher tokenHasher) {
        this.tokenHasher = tokenHasher;
    }


    public String generateEmailToken() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    public String generatePhoneOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }
    // hash refresh token thông qua class tokenhasher
    public String hash(String rawValue) {
        return tokenHasher.hash(rawValue);
    }
}