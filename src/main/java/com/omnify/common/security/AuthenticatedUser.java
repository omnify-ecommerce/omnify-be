package com.omnify.common.security;

import java.util.UUID;

/** Principal gắn vào SecurityContext sau khi JWT được verify thành công. */
public record AuthenticatedUser(UUID userId, UUID companyId, String role) {
}