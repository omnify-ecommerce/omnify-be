package com.omnify.auth.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // NULL = platform admin (Omnify staff). NOT NULL = user thuộc company tương ứng.
    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "email", columnDefinition = "citext")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "locale")
    private String locale = "vi";

    @Column(name = "timezone")
    private String timezone = "Asia/Ho_Chi_Minh";

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "user_status")
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount = 0;

    @Column(name = "last_failed_login_at")
    private OffsetDateTime lastFailedLoginAt;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected User() {
        // JPA
    }

    /** Dùng khi user tự đăng ký -> tạo company mới, user này là owner của company đó. */
    public static User createPending(UUID companyId, String email, String phone,
                                     String passwordHash, String fullName) {
        User user = new User();
        user.companyId = companyId;
        user.email = email;
        user.phone = phone;
        user.passwordHash = passwordHash;
        user.fullName = fullName;
        user.status = UserStatus.PENDING;
        return user;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void markEmailVerified() {
        this.emailVerified = true;
        this.status = UserStatus.ACTIVE;
    }

    public void markPhoneVerified() {
        this.phoneVerified = true;
        this.status = UserStatus.ACTIVE;
    }
    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(OffsetDateTime.now());
    }

    /** Ghi nhận login thất bại; tự khóa tài khoản nếu vượt ngưỡng cho phép. */
    public void registerFailedAttempt(int maxFailedAttempts, int lockDurationMinutes) {
        this.failedLoginCount++;
        this.lastFailedLoginAt = OffsetDateTime.now();
        if (this.failedLoginCount >= maxFailedAttempts) {
            this.lockedUntil = OffsetDateTime.now().plusMinutes(lockDurationMinutes);
        }
    }

    /** Reset trạng thái brute-force sau khi login thành công. */
    public void registerSuccessfulLogin() {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
        this.lastLoginAt = OffsetDateTime.now();
    }

    public OffsetDateTime getLockedUntil() {
        return lockedUntil;
    }
}