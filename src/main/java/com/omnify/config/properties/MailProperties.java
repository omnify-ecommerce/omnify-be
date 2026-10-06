package com.omnify.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "omnify.mail")
public record MailProperties(
    String fromAddress,
    String fromName
) {}
