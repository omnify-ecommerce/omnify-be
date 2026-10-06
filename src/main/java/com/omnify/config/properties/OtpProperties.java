package com.omnify.config.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "omnify.security.otp")
public record OtpProperties(
    int length,
    Duration expiration,
    Duration resendCooldown,
    int maxAttempts
) {}
