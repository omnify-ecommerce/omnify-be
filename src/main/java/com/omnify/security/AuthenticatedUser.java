package com.omnify.security;

import java.util.UUID;


public record AuthenticatedUser(UUID userId, UUID companyId, String role) {
}