package com.omnify.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "omnify.security.token")
public record TokenProperties(
    PasswordReset passwordReset
) {
    
    public record PasswordReset(
        int bytes,
        Duration expiration
    ) {}
}
