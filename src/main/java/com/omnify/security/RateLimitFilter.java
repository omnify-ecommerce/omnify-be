package com.omnify.security;

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
/*Rate limit request theo IP và từng nhóm endpoint (rule)
 chạy đầu tiên trong filter chain đã set bên security config , su dung redis rratelimiter của redissson */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedissonClient redissonClient;
    private final RateLimitProperties properties; //class quyet dinh cau hinh khi ratelimit
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RedissonClient redissonClient, RateLimitProperties properties, ObjectMapper objectMapper) {
        this.redissonClient = redissonClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Endpoint khong khop rule nao da set ben ratelimitproperties -> cho pass
        RateLimitProperties.Rule rule = properties.match(request.getRequestURI());
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }
        // Moi rule +IP co 1 bo diem rieng tren redis tranh dung nhau giua cac endpoint
        String clientIp = resolveClientIp(request);
        String key = "rate_limit:" + rule.name() + ":" + clientIp;

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // SEt rate cho key nay, neu key nay da ton tai tu truoc thi goi lai se khong thuc hien gi
        rateLimiter.trySetRate(RateType.OVERALL, rule.capacity(), rule.refreshPeriodSeconds(), RateIntervalUnit.SECONDS);
        //Dat TTL de redis tu dong don key cua ip inactive
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
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}