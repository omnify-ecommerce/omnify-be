package com.omnify.auth.domain.repository;

import com.omnify.auth.domain.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findTopByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, VerificationToken.Type type);

    @Modifying
    @Query("UPDATE VerificationToken t SET t.attemptCount = t.attemptCount + 1 WHERE t.id = :tokenId")
    void incrementAttemptCount(@Param("tokenId") UUID tokenId);

    @Query("SELECT t.attemptCount FROM VerificationToken t WHERE t.id = :tokenId")
    int findAttemptCountById(@Param("tokenId") UUID tokenId);
}