package com.omnify.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnify.common.exception.ErrorCode;
import com.omnify.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*Do dùng jwt để xác thực nên phải tắt cơ chế đăng nhập mặc định của Spring Security là HTTP Basic và Form Login
nhưng như vậy sẽ phải cấu hình AuthenticationEntryPoint nếu gửi request chưa được xác thực nó sẽ trả 403 mặc định
mà không ghi log nen class này được viết để xử lí trường hợp trên và trả đúng HTTP 401 Unauthorized và response
 JSON thống nhất voi API.*/
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
        HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        log.warn("Unauthenticated access attempt: method={}, uri={}, reason={}",
            request.getMethod(), request.getRequestURI(), authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
            ApiResponse.error(ErrorCode.INVALID_CREDENTIALS.name(), "Yeu cau xac thuc de truy cap duong dan nay")
        ));
    }
}