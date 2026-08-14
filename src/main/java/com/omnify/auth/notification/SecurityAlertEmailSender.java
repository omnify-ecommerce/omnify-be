package com.omnify.auth.notification;

import com.omnify.common.notification.EmailService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SecurityAlertEmailSender {

    private static final Logger log = LoggerFactory.getLogger(SecurityAlertEmailSender.class);
    private static final String COOLDOWN_KEY_PREFIX = "alert_cooldown:duplicate_registration:";

    private final EmailService emailService;
    private final RedissonClient redissonClient;
    private final long cooldownSeconds;

    public SecurityAlertEmailSender(EmailService emailService,
                                    RedissonClient redissonClient,
                                    @Value("${omnify.verification.duplicate-registration-alert-cooldown-seconds}") long cooldownSeconds) {
        this.emailService = emailService;
        this.redissonClient = redissonClient;
        this.cooldownSeconds = cooldownSeconds;
    }

    public void sendDuplicateRegistrationAlert(String toEmail, String fullName) {
        RBucket<String> cooldownBucket = redissonClient.getBucket(COOLDOWN_KEY_PREFIX + toEmail);

        boolean firstAlertInWindow = cooldownBucket.trySet("1", cooldownSeconds, TimeUnit.SECONDS);

        if (!firstAlertInWindow) {
            log.info("Duplicate registration alert suppressed by cooldown for email={}", maskEmail(toEmail));
            return;
        }

        String subject = "Có người cố đăng ký tài khoản Omnify bằng email của bạn";
        String html = buildHtml(fullName);
        emailService.sendHtml(toEmail, subject, html);
    }

    private String buildHtml(String fullName) {
        String greetingName = (fullName == null || fullName.isBlank()) ? "bạn" : fullName;
        return """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                    <h2>Xin chào %s,</h2>
                    <p>Chúng tôi ghi nhận có người vừa cố đăng ký tài khoản Omnify mới bằng email này,
                       trong khi email này đã có tài khoản.</p>
                    <p>Nếu đó là bạn, hãy dùng chức năng <b>đăng nhập</b> hoặc <b>quên mật khẩu</b>
                       thay vì đăng ký lại.</p>
                    <p>Nếu không phải bạn, bạn không cần làm gì thêm — tài khoản của bạn vẫn an toàn.
                       Tuy nhiên nếu lo ngại, bạn nên đổi mật khẩu để chắc chắn.</p>
                </div>
                """.formatted(greetingName);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        return "***" + email.substring(at);
    }
}