package com.omnify.rbac.domain.repository;

import com.omnify.rbac.domain.entity.UserRole;
import com.omnify.rbac.domain.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @Query("SELECT r.name FROM UserRole ur JOIN Role r ON r.id = ur.id.roleId WHERE ur.id.userId = :userId")
    List<String> findRoleNamesByUserId(@Param("userId") UUID userId);
}