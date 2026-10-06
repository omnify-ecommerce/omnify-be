package com.omnify.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@ConfigurationProperties(prefix = "omnify.security.password")
public record PasswordProperties(
    @Min(10) @Max(15) int bcryptStrength
) {}
