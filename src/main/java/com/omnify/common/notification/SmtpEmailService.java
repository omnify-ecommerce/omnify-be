package com.omnify.common.notification;

import com.omnify.config.properties.MailProperties;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

//Class nay de tao va gui html email thong qua SMTP
@Service
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);
    //Javamailsender la thanh phan giao tiep voi SMTP server va gui mimemessage
    private final JavaMailSender mailSender;
    //khai bao cau hinh cua email duoc viet ben class MailProperties
    private final MailProperties mailProperties;

    public SmtpEmailService(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Override
    public void sendHtml(String toAddress, String subject, String htmlBody) {
        try {
            //tao email message co utf8 ho tro tieng viet
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            //thiet lap nguoi gui
            helper.setFrom(mailProperties.getFromAddress(), mailProperties.getFromName());
            //nguoi nhan
            helper.setTo(toAddress);
            //chu de mail da config ben class verificationemailsender
            helper.setSubject(subject);
            //Noi dung email xu li duoi dang html
            helper.setText(htmlBody, true);
            //gui
            mailSender.send(message);
            log.info("Email sent successfully to={}", maskEmail(toAddress));
        } catch (Exception ex) {
            // KHÔNG rethrow: gửi mail thất bại không được làm sập luồng nghiệp vụ gọi nó.
            log.error("Failed to send email to={} subject={}", maskEmail(toAddress), subject, ex);
        }
    }

    //log de debug nhung che mail lai de bao mat
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String masked = local.length() <= 2 ? "**" : local.charAt(0) + "***" + local.charAt(local.length() - 1);
        return masked + email.substring(at);
    }
}