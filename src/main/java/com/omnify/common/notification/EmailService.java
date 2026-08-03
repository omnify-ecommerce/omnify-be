package com.omnify.common.notification;

public interface EmailService {

    /**
     * Gửi email dạng HTML. Implementation KHÔNG được throw exception làm vỡ
     * transaction của caller — mọi lỗi SMTP phải được catch và log nội bộ.
     */
    void sendHtml(String toAddress, String subject, String htmlBody);
}