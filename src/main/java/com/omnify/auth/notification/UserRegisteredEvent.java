package com.omnify.auth.notification;

import com.omnify.auth.domain.entity.VerificationToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class UserRegisteredEvent {

    private final UUID userId;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String rawVerificationToken;
    private final VerificationToken.Type verificationChannel;
}