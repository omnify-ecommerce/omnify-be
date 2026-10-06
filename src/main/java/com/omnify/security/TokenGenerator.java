package com.omnify.security;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class TokenGenerator {

    private static final int TOKEN_LENGTH_BYTES = 32;

    private final SecureRandom secureRandom;
    
    public String generateToken() {
        byte[] bytes = new byte[TOKEN_LENGTH_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
