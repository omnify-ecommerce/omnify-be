package com.omnify.rbac.service;

import com.omnify.rbac.domain.entity.Role;
import com.omnify.rbac.domain.entity.UserRole;
import com.omnify.rbac.domain.repository.RoleRepository;
import com.omnify.rbac.domain.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoleAssignmentServiceImpl implements RoleAssignmentService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public RoleAssignmentServiceImpl(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY) // bắt buộc gọi trong transaction có sẵn của caller (register)
    public void assignRole(UUID userId, String roleName, UUID assignedBy) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException(
                        "Role '" + roleName + "' chưa được seed trong DB "));
        userRoleRepository.save(UserRole.of(userId, role.getId(), assignedBy));
    }
    @Override
    public String getPrimaryRoleName(UUID userId) {
        List<String> roles = userRoleRepository.findRoleNamesByUserId(userId);
        if (roles.isEmpty()) {
            throw new IllegalStateException("User " + userId + " không có role nào được gán");
        }
        return roles.get(0);
    }
}