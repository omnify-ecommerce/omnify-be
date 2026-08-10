package com.omnify.common.notification;

public interface EmailService {
    void sendHtml(String toAddress, String subject, String htmlBody);
}