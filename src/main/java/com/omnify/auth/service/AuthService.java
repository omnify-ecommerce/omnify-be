package com.omnify.auth.service;

import com.omnify.auth.dto.request.LoginRequest;
import com.omnify.auth.dto.request.RegisterRequest;
import com.omnify.auth.dto.response.LoginResponse;
import com.omnify.auth.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);
    void verifyEmail(String email, String otpCode);
    void resendVerificationEmail(String email);
}