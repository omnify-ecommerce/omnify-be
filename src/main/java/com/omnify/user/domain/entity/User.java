package com.omnify.user.domain.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.type.SqlTypes;

import com.omnify.common.entity.AuditableEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OptimisticLock;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SoftDelete(columnName = "is_deleted")
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "citext")
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private UserStatus status = UserStatus.PENDING;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Builder.Default
    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    @Builder.Default
    @OptimisticLock(excluded = true)
    @Column(name = "failed_login_count", nullable = false)
    private Integer failedLoginCount = 0;

    @Column(name = "last_failed_login_at")
    @OptimisticLock(excluded = true)
    private Instant lastFailedLoginAt;

    @Column(name = "locked_until")
    @OptimisticLock(excluded = true)
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    @OptimisticLock(excluded = true)
    private Instant lastLoginAt;

    @Builder.Default
    @Column(name = "is_system", nullable = false)
    private boolean isSystem = false;

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }

    public void markEmailVerified() {
        this.emailVerified = true;
        this.status = UserStatus.ACTIVE;
    }

    public void markPhoneVerified() {
        this.phoneVerified = true;
        this.status = UserStatus.ACTIVE;
    }

    public void registerSuccessfulLogin() {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
        this.lastLoginAt = Instant.now();
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }
}
