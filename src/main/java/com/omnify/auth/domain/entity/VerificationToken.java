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
@Table(name = "verification_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VerificationToken {

    public enum Type {
        EMAIL_VERIFICATION,
        EMAIL_CHANGE,
        PHONE_VERIFICATION,
        PHONE_CHANGE,
        PASSWORD_RESET
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", columnDefinition = "verification_type")
    private Type type;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    // Đếm số lần nhập sai để chống brute-force OTP 6 số
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public static VerificationToken issue(UUID userId, String tokenHash, Type type, OffsetDateTime expiresAt) {
        VerificationToken token = new VerificationToken();
        token.userId = userId;
        token.tokenHash = tokenHash;
        token.type = type;
        token.expiresAt = expiresAt;
        return token;
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markUsed() {
        this.usedAt = OffsetDateTime.now();
    }

    /** Tăng số lần nhập sai. Trả về true nếu đã vượt ngưỡng cho phép. */
    public boolean registerFailedAttempt(int maxAttempts) {
        this.attemptCount++;
        return this.attemptCount >= maxAttempts;
    }
}