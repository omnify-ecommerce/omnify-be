package com.omnify.auth.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.omnify.auth.domain.entity.VerificationToken;
import com.omnify.auth.domain.enums.VerificationType;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    List<VerificationToken> findByTypeAndUsedAtIsNullOrderByCreatedAtDesc(VerificationType type);

    List<VerificationToken> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, VerificationType type);

    Optional<VerificationToken> findTopByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, VerificationType type);

    @Modifying
    @Query("UPDATE VerificationToken t SET t.attemptCount = t.attemptCount + 1 WHERE t.id = :tokenId")
    void incrementAttemptCount(@Param("tokenId") UUID tokenId);

    @Query("SELECT t.attemptCount FROM VerificationToken t WHERE t.id = :tokenId")
    int findAttemptCountById(@Param("tokenId") UUID tokenId);
}
