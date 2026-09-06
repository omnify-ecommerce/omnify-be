package com.omnify.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.omnify.common.constant.SystemUser;

public class SecurityAuditorAware implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Khong co ai dang nhap (vi du: tu dang ky tai khoan) => hanh dong nay do he thong tu thuc hien
        if (authentication == null
            || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of(SystemUser.ID);
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
