package com.omnify.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*Luong xu li : ratelimitfilter de gioi han truy cap -> jwtauthentication filter de xac thuc jwt va set authentication
 -> authorization filter kiem tra xem co duoc phep truy cap endpoint hay khong -> controller xu li nghiep vu */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/api/v1/auth/verify-email",
        "/api/v1/auth/resend-verification",
        "/api/v1/auth/refresh",
        "/api/v1/auth/verify-phone",
        "/api/v1/auth/resend-verification-phone",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter, RateLimitFilter rateLimitFilter,
        RestAuthenticationEntryPoint restAuthenticationEntryPoint
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            //API dùng JWT không dùng cookie đe xác thực nên tạm thời tắt
            .csrf(csrf -> csrf.disable())
            //JWT  authentication là stateless, không lưu authentication vào Httpsession.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            //PermitAll các đường dẫn endpoint public ở trên
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            )
            // tắt 2 cơ chế đăng nhập mặc định của Spring vì không phù hợp với JWT stateless
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            /* do tắt 2 co chế trên nên phải đăng ký class xử lí thay thế fallback mặc đinh nếu ko sẽ trả 403
             mà không có log khi có request chưa được xác thực đi vào */
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(restAuthenticationEntryPoint))
            // chèn jwt authetication vào trước filter đăng nhập mặc định của Spring
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // chèn rate limiter trước jwt authentication để chặn spam
            .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}