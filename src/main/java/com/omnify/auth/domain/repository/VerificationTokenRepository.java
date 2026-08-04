package com.omnify.auth.domain.repository;

import com.omnify.auth.domain.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    
    Optional<VerificationToken> findTopByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, VerificationToken.Type type);
}