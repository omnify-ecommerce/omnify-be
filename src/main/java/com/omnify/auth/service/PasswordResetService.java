package com.omnify.auth.service;

import com.omnify.auth.dto.request.ForgotPasswordRequest;
import com.omnify.auth.dto.request.ResetPasswordRequest;

public interface PasswordResetService {

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
