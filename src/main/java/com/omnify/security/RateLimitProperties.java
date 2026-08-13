package com.omnify.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "omnify.security.rate-limit")
public class RateLimitProperties {

    private Map<String, Rule> rules = Map.of();

    public Map<String, Rule> getRules() {
        return rules;
    }

    public void setRules(Map<String, Rule> rules) {
        this.rules = rules;
    }

    /** Match theo suffix của URI - đỡ phải hard-code full path context. */
    public Rule match(String requestUri) {
        return rules.values().stream()
                .filter(rule -> requestUri.endsWith(rule.path()))
                .findFirst()
                .orElse(null);
    }

    public record Rule(String path, int capacity, long refreshPeriodSeconds) {
        /** Dùng làm suffix cho Redis key, cho dễ đọc trên RedisInsight. */
        public String name() {
            return path.replace("/", "_");
        }
    }
}