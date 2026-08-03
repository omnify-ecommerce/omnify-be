package com.omnify.rbac.service;

import java.util.UUID;

/**
 * Public contract của module RBAC dành cho các module khác (auth, user...) gọi vào.
 * Module khác KHÔNG được truy cập trực tiếp RoleRepository/UserRoleRepository
 * của rbac để giữ đúng nguyên tắc DDD strict isolation.
 */
public interface RoleAssignmentService {

    void assignRole(UUID userId, String roleName, UUID assignedBy);
    String getPrimaryRoleName(UUID userId);
}