package com.omnify.auth.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class MockPhoneOtpSender implements PhoneOtpSender {
    private static final Logger log = LoggerFactory.getLogger(MockPhoneOtpSender.class);

    @Override
    public void sendOtp(String phone, String fullName, String otpCode) {
        log.info("Gui OTP {} den so {} voi user la {}", otpCode, phone, fullName);
    }
}
