package com.omnify.auth.infrastructure;

public interface CaptchaVerifier {
    CaptchaResult verify(String captchaToken);
}
