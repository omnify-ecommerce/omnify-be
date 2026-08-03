package com.omnify.auth.service;

import com.omnify.auth.domain.entity.VerificationToken;
import com.omnify.auth.domain.event.UserRegisteredEvent;
import com.omnify.auth.notification.VerificationEmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredEventListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredEventListener.class);

    private final VerificationEmailSender verificationEmailSender;

    public UserRegisteredEventListener(VerificationEmailSender verificationEmailSender) {
        this.verificationEmailSender = verificationEmailSender;
    }

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        if (event.getVerificationChannel() != VerificationToken.Type.EMAIL_VERIFY) {
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