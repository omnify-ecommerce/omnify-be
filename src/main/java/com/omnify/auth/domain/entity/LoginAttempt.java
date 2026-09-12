package com.omnify.auth.domain.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.omnify.auth.domain.enums.LoginFailureReason;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "login_attempts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String identifier;

    @Column(nullable = false)
    private Boolean success;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "failure_reason")
    private LoginFailureReason failureReason;

    @Column(name = "ip_address", columnDefinition = "inet", nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static LoginAttempt success(
            UUID userId,
            String identifier,
            String ipAddress,
            String userAgent
    ) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.userId = userId;
        attempt.identifier = identifier;
        attempt.success = true;
        attempt.failureReason = null;
        attempt.ipAddress = ipAddress;
        attempt.userAgent = userAgent;
        return attempt;
    }

    public static LoginAttempt failure(
            UUID userId,
            String identifier,
            LoginFailureReason failureReason,
            String ipAddress,
            String userAgent
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
}
