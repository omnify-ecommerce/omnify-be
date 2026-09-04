package com.omnify.auth.infrastructure;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

//Verify reCAPTCHA v2 (checkbox challenge), dung lam buoc step-up khi v3 tra ve score thap
@Component("recaptchaV2Verifier")
public class GoogleRecaptchaV2Verifier implements CaptchaVerifier {
    private static final Logger log = LoggerFactory.getLogger(GoogleRecaptchaV2Verifier.class);
    private final RestClient restClient = RestClient.create();
    private final String secretKey;
    private final String verifyUrl;

    public GoogleRecaptchaV2Verifier(
        @Value("${omnify.captcha.recaptcha.v2-secret-key}") String secretKey,
        @Value("${omnify.captcha.recaptcha.verify-url}") String verifyUrl
    ) {
        this.secretKey = secretKey;
        this.verifyUrl = verifyUrl;
    }

    @Override
    public CaptchaResult verify(String captchaToken) {
        if (captchaToken == null || captchaToken.isBlank()) {
            log.warn("Captcha v2 token rong");
            return CaptchaResult.FAILED;
        }
        try {
            String body = "secret=" + URLEncoder.encode(secretKey, StandardCharsets.UTF_8)
                + "&response=" + URLEncoder.encode(captchaToken, StandardCharsets.UTF_8);
            SiteVerifyResponse response = restClient.post()
                .uri(verifyUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(SiteVerifyResponse.class);

            //v2 khong co score, chi can success la coi nhu user da vuot qua challenge
            if (response == null || !response.success()) {
                log.warn("Captcha v2 verify that bai: {}", response);
                return CaptchaResult.FAILED;
            }
            log.info("Captcha v2 verify thanh cong");
            return CaptchaResult.PASSED;
        } catch (Exception e) {
            log.error("Loi khi goi Google site verify (v2)", e);
            return CaptchaResult.FAILED;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SiteVerifyResponse(
        boolean success,
        String hostname,
        @JsonProperty("error-codes") List<String> errorCodes) {
    }
}
