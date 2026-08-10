package com.omnify.common.security;

import java.util.UUID;


public record AuthenticatedUser(UUID userId, UUID companyId, String role) {
}