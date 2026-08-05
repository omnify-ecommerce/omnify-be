package com.omnify.auth.service;

import com.omnify.auth.domain.entity.LoginAttempt;
import com.omnify.auth.domain.entity.RefreshToken;
import com.omnify.auth.domain.repository.LoginAttemptRepository;
import com.omnify.auth.domain.repository.RefreshTokenRepository;
import com.omnify.auth.domain.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ghi nhận login thất bại trong transaction RIÊNG (REQUIRES_NEW), commit độc lập
 * NGAY LẬP TỨC — không phụ thuộc vào việc transaction gọi nó (login()) sẽ rollback
 * do throw BusinessException ngay sau đó. Nếu không tách riêng, Spring sẽ rollback
 * luôn cả phần tăng failed_login_count vì nó nằm chung transaction với exception.
 */
@Component
public class LoginSecurityRecorder {

    private final UserRepository userRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginSecurityRecorder(UserRepository userRepository,
                                 LoginAttemptRepository loginAttemptRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(UUID userId, String identifier, String ipAddress, String userAgent,
                                    int maxFailedAttempts, int lockDurationMinutes) {
        OffsetDateTime lockUntil = OffsetDateTime.now().plusMinutes(lockDurationMinutes);
        userRepository.incrementFailedLoginAndMaybeLock(userId, maxFailedAttempts, lockUntil);
        loginAttemptRepository.save(LoginAttempt.record(userId, identifier, false, ipAddress, userAgent));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordUnknownIdentifierAttempt(String identifier, String ipAddress, String userAgent) {
        loginAttemptRepository.save(LoginAttempt.record(null, identifier, false, ipAddress, userAgent));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLockedAttempt(UUID userId, String identifier, String ipAddress, String userAgent) {
        loginAttemptRepository.save(LoginAttempt.record(userId, identifier, false, ipAddress, userAgent));
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllSessionsOnReuseDetected(java.util.UUID userId) {
        refreshTokenRepository.revokeAllSessionsByUserId(
                userId, RefreshToken.Status.VALID, RefreshToken.Status.REVOKED);
    }
}