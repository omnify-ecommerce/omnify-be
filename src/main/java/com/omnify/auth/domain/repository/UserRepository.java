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

    /**
     * UPDATE atomic trong 1 câu lệnh duy nhất — Postgres tự đảm bảo tính atomic
     * của 1 statement UPDATE trên 1 row mà không cần SELECT ... FOR UPDATE giữ lock
     * xuyên suốt transaction. Tránh được cả 2 vấn đề: (1) rollback theo transaction cha
     * khi login fail throw exception, (2) deadlock nếu kết hợp pessimistic lock với
     * REQUIRES_NEW.
     */
    @Modifying
    @Query("UPDATE User u SET " +
            "u.failedLoginCount = u.failedLoginCount + 1, " +
            "u.lastFailedLoginAt = CURRENT_TIMESTAMP, " +
            "u.lockedUntil = CASE WHEN u.failedLoginCount + 1 >= :maxAttempts THEN :lockUntil ELSE u.lockedUntil END " +
            "WHERE u.id = :userId")
    void incrementFailedLoginAndMaybeLock(@Param("userId") UUID userId,
                                          @Param("maxAttempts") int maxAttempts,
                                          @Param("lockUntil") OffsetDateTime lockUntil);
}