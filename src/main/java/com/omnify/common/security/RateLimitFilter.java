package com.omnify.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnify.common.exception.ErrorCode;
import com.omnify.common.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedissonClient redissonClient;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RedissonClient redissonClient, RateLimitProperties properties, ObjectMapper objectMapper) {
        this.redissonClient = redissonClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        RateLimitProperties.Rule rule = properties.match(request.getRequestURI());
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        String key = "rate_limit:" + rule.name() + ":" + clientIp;

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // trySetRate chỉ có tác dụng lần đầu tạo key trên Redis, gọi lại vô hại (idempotent)
        rateLimiter.trySetRate(RateType.OVERALL, rule.capacity(), rule.refreshPeriodSeconds(), RateIntervalUnit.SECONDS);
        // set TTL để key tự dọn rác, tránh Redis phình to vô hạn vì IP vãng lai
        rateLimiter.expire(Duration.ofSeconds(rule.refreshPeriodSeconds() * 2));

        if (!rateLimiter.tryAcquire(1)) {
            response.setStatus(ErrorCode.RATE_LIMIT_EXCEEDED.getHttpStatus().value());
            response.setContentType("application/json;charset=UTF-8");
            ApiResponse<Void> body = ApiResponse.error(
                    ErrorCode.RATE_LIMIT_EXCEEDED.name(),
                    ErrorCode.RATE_LIMIT_EXCEEDED.getDefaultMessage());
            response.getWriter().write(objectMapper.writeValueAsString(body));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        // CHỈ tin X-Forwarded-For khi có LB/reverse-proxy đứng trước xóa header client tự set.
        // Local/dev chưa có proxy thì cứ getRemoteAddr() cho chắc, đừng trust header mù quáng.
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}