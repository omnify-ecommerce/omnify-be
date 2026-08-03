package com.omnify.auth.notification;

import com.omnify.common.config.MailProperties;
import com.omnify.common.notification.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VerificationEmailSender {

    private final EmailService emailService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${app.frontend.verify-email-path}")
    private String verifyEmailPath;

    public VerificationEmailSender(EmailService emailService, MailProperties mailProperties) {
        this.emailService = emailService;
    }

    public void sendVerificationEmail(String toEmail, String fullName, String rawToken) {
        String verifyUrl = frontendBaseUrl + verifyEmailPath + "?token=" + rawToken;
        String subject = "Xác thực tài khoản Omnify";
        String html = buildHtml(fullName, verifyUrl);
        emailService.sendHtml(toEmail, subject, html);
    }

    private String buildHtml(String fullName, String verifyUrl) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                    <h2>Xin chào %s,</h2>
                    <p>Vui lòng bấm vào nút bên dưới để xác thực tài khoản Omnify của bạn.
                    Liên kết có hiệu lực trong 30 phút.</p>
                    <p style="text-align:center; margin: 24px 0;">
                        <a href="%s" style="background:#2563eb; color:#fff; padding:12px 24px;
                        text-decoration:none; border-radius:6px; display:inline-block;">
                            Xác thực tài khoản
                        </a>
                    </p>
                    <p>Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.</p>
                </div>
                """.formatted(fullName, verifyUrl);
    }
}