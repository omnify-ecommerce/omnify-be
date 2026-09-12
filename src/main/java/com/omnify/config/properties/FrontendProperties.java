package com.omnify.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "omnify.frontend")
public record FrontendProperties(
    String baseUrl
) {}
