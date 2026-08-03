package com.omnify.auth.domain.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "login_attempts")
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "identifier", nullable = false)
    private String identifier;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected LoginAttempt() {
        // JPA
    }

    public static LoginAttempt record(UUID userId, String identifier, boolean success,
                                      String ipAddress, String userAgent) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.userId = userId;
        attempt.identifier = identifier;
        attempt.success = success;
        attempt.ipAddress = ipAddress;
        attempt.userAgent = userAgent;
        return attempt;
    }
}