package com.omnify.auth.infrastructure;

public interface CaptchaVerifier {
    boolean verify(String captchaToken);
}
