package com.omnify.auth.notification;

import com.omnify.user.domain.entity.UserProfile;
import com.omnify.user.domain.repository.UserProfileRepository;
import com.omnify.user.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class DuplicateRegistrationEventListener {

    private static final Logger log = LoggerFactory.getLogger(DuplicateRegistrationEventListener.class);

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final SecurityAlertEmailSender securityAlertEmailSender;

    public DuplicateRegistrationEventListener(
        UserRepository userRepository, UserProfileRepository userProfileRepository,
        SecurityAlertEmailSender securityAlertEmailSender
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.securityAlertEmailSender = securityAlertEmailSender;
    }

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void onDuplicateRegistration(DuplicateRegistrationEvent event) {
        userRepository.findByEmail(event.getEmail()).ifPresentOrElse(
            user -> {
                String fullName= userProfileRepository.findByUserId(user.getId())
                    .map(profile -> profile.getFirstName() + " " + profile.getLastName())
                    .orElse(user.getEmail());
                securityAlertEmailSender.sendDuplicateRegistrationAlert(user.getEmail(), fullName);
            },
            () -> log.warn("Duplicate registration alert fired but user not found, email may have changed. email={}",
                maskEmail(event.getEmail()))
        );
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        return "***" + email.substring(at);
    }
}