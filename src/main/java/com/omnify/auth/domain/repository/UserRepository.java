package com.omnify.auth.domain.repository;

import com.omnify.auth.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.phone = :identifier")
    Optional<User> findByEmailOrPhone(@Param("identifier") String identifier);

    @Modifying
    @Query("UPDATE User u SET " +
            "u.failedLoginCount = u.failedLoginCount + 1, " +
            "u.lastFailedLoginAt = CURRENT_TIMESTAMP " +
            "WHERE u.id = :userId")
    void incrementFailedLoginCount(@Param("userId") UUID userId);

    @Query("SELECT u.failedLoginCount FROM User u WHERE u.id = :userId")
    int findFailedLoginCount(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void setLockedUntil(@Param("userId") UUID userId, @Param("lockedUntil") OffsetDateTime lockedUntil);
}