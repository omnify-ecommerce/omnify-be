package com.omnify.auth.domain.event;

import com.omnify.auth.domain.entity.VerificationToken;

import java.util.UUID;

public class UserRegisteredEvent {

    private final UUID userId;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String rawVerificationToken;
    private final VerificationToken.Type verificationChannel;

    public UserRegisteredEvent(UUID userId, String fullName, String email, String phone,
                               String rawVerificationToken, VerificationToken.Type verificationChannel) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.rawVerificationToken = rawVerificationToken;
        this.verificationChannel = verificationChannel;
    }

    public UUID getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRawVerificationToken() { return rawVerificationToken; }
    public VerificationToken.Type getVerificationChannel() { return verificationChannel; }
}