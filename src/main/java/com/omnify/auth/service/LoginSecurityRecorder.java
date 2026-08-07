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

    // Sai mat khau
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(UUID userId, String identifier, String ipAddress, String userAgent,
                                    int maxFailedAttempts, int lockDurationMinutes) {
        OffsetDateTime lockUntil = OffsetDateTime.now().plusMinutes(lockDurationMinutes);
        userRepository.incrementFailedLoginAndMaybeLock(userId, maxFailedAttempts, lockUntil);
        loginAttemptRepository.save(LoginAttempt.failure(
                userId, identifier, ipAddress, userAgent, LoginAttempt.FailureReason.INVALID_CREDENTIALS));
    }

    // acc khong ton tai
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordUnknownIdentifierAttempt(String identifier, String ipAddress, String userAgent) {
        loginAttemptRepository.save(LoginAttempt.failure(
                null, identifier, ipAddress, userAgent, LoginAttempt.FailureReason.ACCOUNT_NOT_FOUND));
    }

    //tai khoan bi khoa do nhap sai nhieu lan
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLockedAttempt(UUID userId, String identifier, String ipAddress, String userAgent) {
        loginAttemptRepository.save(LoginAttempt.failure(
                userId, identifier, ipAddress, userAgent, LoginAttempt.FailureReason.ACCOUNT_LOCKED));
    }

    // tai khoan chua xac thuc
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordUnverifiedAttempt(UUID userId, String identifier, String ipAddress, String userAgent,
                                        LoginAttempt.FailureReason reason) {
        loginAttemptRepository.save(LoginAttempt.failure(userId, identifier, ipAddress, userAgent, reason));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllSessionsOnReuseDetected(java.util.UUID userId) {
        refreshTokenRepository.revokeAllSessionsByUserId(
                userId, RefreshToken.Status.VALID, RefreshToken.Status.REVOKED);
    }
}