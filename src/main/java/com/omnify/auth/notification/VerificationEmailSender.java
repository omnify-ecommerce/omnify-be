package com.omnify.auth.notification;

import com.omnify.common.notification.EmailService;
import org.springframework.stereotype.Component;

//class nay se tao subject va noi dung cua email dang ky sau do chuyen viec gui mail cho email service
@Component
public class VerificationEmailSender {

    private final EmailService emailService;

    public VerificationEmailSender(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendVerificationEmail(String toEmail, String fullName, String otpCode) {
        String subject = "Mã xác thực tài khoản Omnify";
        String html = buildHtml(fullName, otpCode);
        emailService.sendHtml(toEmail, subject, html);
    }

    private String buildHtml(String fullName, String otpCode) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;">
                <h2>Xin chào %s,</h2>
                <p>Mã xác thực tài khoản Omnify của bạn là:</p>
                <p style="font-size: 28px; font-weight: bold; letter-spacing: 6px;
                   text-align: center; margin: 24px 0; color:#2563eb;">%s</p>
                <p>Mã có hiệu lực trong 30 phút. Vui lòng nhập mã này vào ứng dụng để hoàn tất xác thực.</p>
                <p>Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.</p>
            </div>
            """.formatted(fullName, otpCode);
    }
}