package com.omnify.auth.infrastructure;

public interface CaptchaCarrier {
    String getCaptchaTokenV3();
    String getCaptchaTokenV2();
}
