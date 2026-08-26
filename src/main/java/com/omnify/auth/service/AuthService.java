package com.omnify.auth.service;

import com.omnify.auth.dto.request.LoginRequest;
import com.omnify.auth.dto.request.RegisterRequest;
import com.omnify.auth.dto.response.LoginResponse;
import com.omnify.auth.dto.response.RegisterResponse;

import java.util.UUID;

public interface AuthService {

    RegisterResponse register(RegisterRequest request, String ipAddress);

    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);

    void verifyEmail(String email, String otpCode);

    void resendVerificationEmail(String email);

    LoginResponse refreshToken(String rawRefreshToken, String ipAddress, String userAgent);

    void logout(String rawRefreshToken, UUID userId);

    void verifyPhone(String phone, String otpCode);

    void resendVerificationPhone(String phone);
}