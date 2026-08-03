package com.omnify.rbac.domain.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_roles")
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @Column(name = "assigned_by")
    private UUID assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    protected UserRole() {
        // JPA
    }

    public static UserRole of(UUID userId, UUID roleId, UUID assignedBy) {
        UserRole userRole = new UserRole();
        userRole.id = new UserRoleId(userId, roleId);
        userRole.assignedBy = assignedBy;
        userRole.assignedAt = OffsetDateTime.now();
        return userRole;
    }

    public UserRoleId getId() {
        return id;
    }
}