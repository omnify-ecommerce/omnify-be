package com.omnify.auth.service;

import com.omnify.auth.domain.entity.VerificationToken;
import com.omnify.auth.domain.repository.VerificationTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class VerificationAttemptRecorder {

    private final VerificationTokenRepository verificationTokenRepository;

    public VerificationAttemptRecorder(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    /**
     * Ghi nhận lần verify OTP sai, chạy trong transaction ĐỘC LẬP để không bị
     * rollback theo transaction chính khi service ném BusinessException.
     * Dùng UPDATE atomic thay vì load-mutate-save để tránh lost update khi
     * nhiều request verify song song trên cùng 1 token.
     *
     * @return attemptCount SAU khi tăng, để service quyết định có OTP_LOCKED hay không
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedAttempt(java.util.UUID tokenId) {
        verificationTokenRepository.incrementAttemptCount(tokenId);
        return verificationTokenRepository.findAttemptCountById(tokenId);
    }
}