package com.omnify.auth.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "login_attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "failure_reason", columnDefinition = "login_failure_reason")
    private FailureReason failureReason;
    @Column(name = "ip_address", columnDefinition = "INET", nullable = false)
    @JdbcTypeCode(SqlTypes.INET)
    private String ipAddress;
    @Column(name = "user_agent")
    private String userAgent;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public static LoginAttempt success(UUID userId, String identifier, String ipAddress, String userAgent) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.userId = userId;
        attempt.identifier = identifier;
        attempt.success = true;
        attempt.ipAddress = ipAddress;
        attempt.userAgent = userAgent;
        return attempt;
    }

    public static LoginAttempt failure(
        UUID userId, String identifier, String ipAddress, String userAgent,
        FailureReason failureReason
    ) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.userId = userId;
        attempt.identifier = identifier;
        attempt.success = false;
        attempt.failureReason = failureReason;
        attempt.ipAddress = ipAddress;
        attempt.userAgent = userAgent;
        return attempt;
    }


    public enum FailureReason {
        INVALID_CREDENTIALS,
        ACCOUNT_NOT_FOUND,
        ACCOUNT_LOCKED,
        ACCOUNT_DISABLED,
        EMAIL_NOT_VERIFIED,
        PHONE_NOT_VERIFIED,
        MFA_FAILED
    }
}