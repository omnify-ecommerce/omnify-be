package com.omnify.auth.service.recorder;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailedAttempt(java.util.UUID tokenId) {
        verificationTokenRepository.incrementAttemptCount(tokenId);
        return verificationTokenRepository.findAttemptCountById(tokenId);
    }
}