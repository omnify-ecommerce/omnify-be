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
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    public enum Status {
        VALID, EXPIRED, REVOKED
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
    @Column(name = "status", nullable = false, columnDefinition = "token_status")
    private Status status = Status.VALID;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address", columnDefinition = "INET", nullable = false)
    @JdbcTypeCode(SqlTypes.INET)
    private String ipAddress;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    public static RefreshToken issue(UUID userId, String tokenHash, String deviceName,
                                     String userAgent, String ipAddress, OffsetDateTime expiresAt) {
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
        return status == Status.VALID && expiresAt.isAfter(OffsetDateTime.now());
    }

    public void revoke() {
        this.status = Status.REVOKED;
        this.revokedAt = OffsetDateTime.now();
    }

    public void touchLastUsed() {
        this.lastUsedAt = OffsetDateTime.now();
    }
}