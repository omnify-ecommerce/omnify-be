package com.omnify.auth.notification;

import com.omnify.auth.domain.entity.VerificationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;
//class nay de kiem tra xem neu transaction cua luong email thanh cong chua va valid 1 so thong tin, neu
// thanh cong chuyen du lieu sach cho verificationemailsender tiep tuc xu li
@Component
public class UserRegisteredEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventListener.class);

    private final VerificationEmailSender verificationEmailSender;

    public UserRegisteredEventListener(VerificationEmailSender verificationEmailSender) {
        this.verificationEmailSender = verificationEmailSender;
    }
    //su dung async de tao 1 thread rieng voi luong chinh, va @TransactionalEventListener giup doan code chay khi
    // publish 1 userRegisteredEvent
    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        if (event.getVerificationChannel() != VerificationToken.Type.EMAIL_VERIFICATION) {
            log.info("Verification channel is not email, skip sending mail. userId={}", event.getUserId());
            return;
        }
        if (event.getEmail() == null) {
            log.warn("email_verify channel but email is null, userId={}", event.getUserId());
            return;
        }
        verificationEmailSender.sendVerificationEmail(
                event.getEmail(),
                event.getFullName(),
                event.getRawVerificationToken()
        );
    }
}