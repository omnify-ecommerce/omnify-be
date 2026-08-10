package com.omnify.auth.domain.repository;

import com.omnify.auth.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("SELECT rt FROM RefreshToken rt " +
            "WHERE rt.userId = :userId AND rt.status = :status " +
            "AND rt.expiresAt > CURRENT_TIMESTAMP " +
            "ORDER BY rt.lastUsedAt DESC NULLS LAST")
    List<RefreshToken> findActiveSessionsByUserId(@Param("userId") UUID userId,
                                                  @Param("status") RefreshToken.Status status);


    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.status = :revokedStatus, rt.revokedAt = CURRENT_TIMESTAMP " +
            "WHERE rt.id = :sessionId AND rt.userId = :userId AND rt.status = :validStatus")
    int revokeSession(@Param("sessionId") UUID sessionId,
                      @Param("userId") UUID userId,
                      @Param("validStatus") RefreshToken.Status validStatus,
                      @Param("revokedStatus") RefreshToken.Status revokedStatus);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.status = :revokedStatus, rt.revokedAt = CURRENT_TIMESTAMP " +
            "WHERE rt.userId = :userId AND rt.id <> :currentSessionId AND rt.status = :validStatus")
    int revokeAllOtherSessions(@Param("userId") UUID userId,
                               @Param("currentSessionId") UUID currentSessionId,
                               @Param("validStatus") RefreshToken.Status validStatus,
                               @Param("revokedStatus") RefreshToken.Status revokedStatus);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    Optional<RefreshToken> findByTokenHash(String tokenHash);


    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.status = :revokedStatus, rt.revokedAt = CURRENT_TIMESTAMP " +
            "WHERE rt.userId = :userId AND rt.status = :validStatus")
    int revokeAllSessionsByUserId(@Param("userId") UUID userId,
                                  @Param("validStatus") RefreshToken.Status validStatus,
                                  @Param("revokedStatus") RefreshToken.Status revokedStatus);
}