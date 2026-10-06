package com.omnify.common.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.omnify.config.properties.MailProperties;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);
    // JavaMailSender là thành phần giao tiếp với SMTP server và gửi MimeMessage
    private final JavaMailSender mailSender;
    // Khai báo cấu hình của email được viết bên class MailProperties
    private final MailProperties mailProperties;

    @Override
    public void sendHtml(String toAddress, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(mailProperties.fromAddress(), mailProperties.fromName());
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent successfully to={}", maskEmail(toAddress));
        } catch (Exception ex) {
            // KHÔNG rethrow: gửi mail thất bại không được làm sập luồng nghiệp vụ gọi nó.
            log.error("Failed to send email to={} subject={}", maskEmail(toAddress), subject, ex);
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String masked = local.length() <= 2 ? "**" : local.charAt(0) + "***" + local.charAt(local.length() - 1);
        return masked + email.substring(at);
    }
}
