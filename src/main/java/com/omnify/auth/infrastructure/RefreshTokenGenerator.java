package com.omnify.auth.infrastructure;

import com.omnify.security.TokenHasher;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class RefreshTokenGenerator {

    private final SecureRandom secureRandom = new SecureRandom();
    private final TokenHasher tokenHasher;

    public RefreshTokenGenerator(TokenHasher tokenHasher) {
        this.tokenHasher = tokenHasher;
    }

    // Refresh token 256-bit, random
    public String generate() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    //hash refresh token thông qua class tokenhasher
    public String hash(String rawValue) {
        return tokenHasher.hash(rawValue);
    }
}