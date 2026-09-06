package com.omnify.auth.infrastructure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "omnify.security.auth.captcha-gate")
public class CaptchaGateProperties {
    // so lan fail toi da truoc khi bat buoc nhap captcha
    private int threshold = 3;
    //thoi gian tinh tu lan fail dau tien (giay)
    private long windowSeconds= 900;
}
