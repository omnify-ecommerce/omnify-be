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


@Component
public class GoogleRecaptchaV3Verifier implements CaptchaVerifier {
    private static final Logger log = LoggerFactory.getLogger(GoogleRecaptchaV3Verifier.class);
    private final RestClient restClient = RestClient.create();
    private final String secretKey;
    private final String verifyUrl;
    private final double scoreThreshold;

    public GoogleRecaptchaV3Verifier(
        @Value("${omnify.captcha.recaptcha.secret-key}") String secretKey,
        @Value("${omnify.captcha.recaptcha.verify-url}") String verifyUrl,
        @Value("${omnify.captcha.recaptcha.score-threshold}") double scoreThreshold
    ) {
        this.secretKey = secretKey;
        this.verifyUrl = verifyUrl;
        this.scoreThreshold = scoreThreshold;
    }

    @Override
    public boolean verify(String captchaToken) {
        if (captchaToken == null || captchaToken.isBlank()) {
            log.warn("Captcha token rong");
            return false;
        }
        try {
            //dung content type la application/x-www-form-urlencoded theo suggest cua docs siteverify cua google
            //content type dang application/x-www-form-urlencoded
            String body = "secret=" + URLEncoder.encode(secretKey, StandardCharsets.UTF_8)
                + "&response=" + URLEncoder.encode(captchaToken, StandardCharsets.UTF_8);
            SiteVerifyResponse response = restClient.post()
                .uri(verifyUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(SiteVerifyResponse.class);

            if (response == null || !response.success()) {
                log.warn("Captcha verify that bai: {}", response);
                return false;
            }

            if (response.score() == null) {
                log.warn("Captcha v3 response khong co score");
                return false;
            }

            if (response.score() < scoreThreshold) {
                log.warn("Captcha score {} thap hon nguong quy dinh la {}", response.score(), scoreThreshold);
                return false;
            }
            log.info("Captcha verify thanh cong. Score dat duoc: {}", response.score());
            return true;
        } catch (Exception e) {
            log.error("Loi khi goi Google site verify", e);
            return false;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SiteVerifyResponse(
        boolean success,
        Double score,
        String action,
        String hostName,
        @JsonProperty("error-codes") List<String> errorCodes) {
    }
}
