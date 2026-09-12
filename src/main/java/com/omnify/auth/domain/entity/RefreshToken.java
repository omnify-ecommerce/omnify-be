package com.omnify.auth.domain.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.omnify.auth.domain.enums.TokenStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_tokens")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private TokenStatus status = TokenStatus.VALID;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column(name = "ip_address", columnDefinition = "inet")
    private String ipAddress;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static RefreshToken issue(
            UUID userId,
            String tokenHash,
            String deviceName,
            String userAgent,
            String ipAddress,
            Instant expiresAt
    ) {
        RefreshToken token = new RefreshToken();
        token.userId = userId;
        token.tokenHash = tokenHash;
        token.deviceName = deviceName;
        token.userAgent = userAgent;
        token.ipAddress = ipAddress;
        token.expiresAt = expiresAt;
        return token;
    }

    public boolean isActive() {
        return status == TokenStatus.VALID && expiresAt.isAfter(Instant.now());
    }

    public void revoke() {
        this.status = TokenStatus.REVOKED;
        this.revokedAt = Instant.now();
    }

    public void touchLastUsed() {
        this.lastUsedAt = Instant.now();
    }
}
