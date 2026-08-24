package com.omnify.auth.notification;

import com.omnify.auth.domain.entity.VerificationToken;
import com.omnify.auth.infrastructure.PhoneOtpSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

//class nay de kiem tra xem neu transaction cua luong email thanh cong chua va valid 1 so thong tin, neu
// thanh cong chuyen du lieu sach cho verificationemailsender tiep tuc xu li
@Component
public class UserRegisteredEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventListener.class);

    private final VerificationEmailSender verificationEmailSender;
    private final PhoneOtpSender phoneOtpSender;

    public UserRegisteredEventListener(VerificationEmailSender verificationEmailSender, PhoneOtpSender phoneOtpSender) {
        this.verificationEmailSender = verificationEmailSender;
        this.phoneOtpSender = phoneOtpSender;
    }

    //su dung async de tao 1 thread rieng voi luong chinh, va @TransactionalEventListener giup doan code chay khi
    // publish 1 userRegisteredEvent
    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        if (event.getVerificationChannel() == VerificationToken.Type.EMAIL_VERIFICATION) {
            if (event.getEmail() == null) {
                log.warn("email_verify channel but email is null, userId={}", event.getUserId());
            }
            verificationEmailSender.sendVerificationEmail(
                event.getEmail(),
                event.getFullName(),
                event.getRawVerificationToken()
            );
            return;
        }
        if (event.getVerificationChannel() == VerificationToken.Type.PHONE_VERIFICATION) {
            if (event.getPhone() == null) {
                log.warn("phone_verify channel but phone is null, userId={}", event.getUserId());
            }
            phoneOtpSender.sendOtp(
                event.getPhone(),
                event.getFullName(),
                event.getRawVerificationToken()
            );
            return;
        }
        log.info("Verification not found {}, skip userId={}", event.getVerificationChannel(), event.getUserId());
    }
}