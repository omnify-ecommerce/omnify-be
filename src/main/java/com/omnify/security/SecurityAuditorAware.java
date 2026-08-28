package com.omnify.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityAuditorAware implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
            || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

//        if (!(principal instanceof CustomUserPrincipal customUserPrincipal)) {
//            return Optional.empty();
//        }
//
//        return Optional.of(customUserPrincipal.getUserId());
        //sua logic theo huong giu ca 2
        if (principal instanceof AuthenticatedUser authenticatedUser){
            return Optional.of(authenticatedUser.userId());
        }

        if (principal instanceof CustomUserPrincipal customUserPrincipal){
            return Optional.of(customUserPrincipal.getUserId());
        }

        return Optional.empty();

    }
}
