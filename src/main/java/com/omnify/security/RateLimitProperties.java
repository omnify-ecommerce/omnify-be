package com.omnify.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/* Class nay duoc tao de doc cau hinh tu application.yml tach thanh conffig rieng
 ko hard code de co the sua doi de dang */
@Getter
@Setter
@ConfigurationProperties(prefix = "omnify.security.rate-limit")
public class RateLimitProperties {

    private Map<String, Rule> rules = Map.of();

    // kiem tra duong dan request co match voi rule nao khong
    public Rule match(String requestUri) {
        return rules.values().stream()
            .filter(rule -> requestUri.endsWith(rule.path()))
            .findFirst()
            .orElse(null);
    }

    //kieu rule
    public record Rule(String path, int capacity, long refreshPeriodSeconds) {
        //doi path tu / thanh - de de doc trong redis
        public String name() {
            return path.replace("/", "_");
        }
    }
}