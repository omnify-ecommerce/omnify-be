package com.omnify.auth.service;

import com.omnify.auth.dto.request.LoginRequest;
import com.omnify.auth.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
