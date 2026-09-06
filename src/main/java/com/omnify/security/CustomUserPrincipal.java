package com.omnify.security;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.omnify.user.domain.entity.User;
import com.omnify.user.domain.entity.UserStatus;

import lombok.Getter;

@Getter
public class CustomUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonLocked;

    public CustomUserPrincipal(
        UUID userId,
        String username,
        String password,
        Collection<? extends GrantedAuthority> authorities,
        boolean enabled,
        boolean accountNonLocked
    ) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
    }

    public static CustomUserPrincipal fromUser(User user) {
        return new CustomUserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getPasswordHash(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
            user.getStatus() == UserStatus.ACTIVE,
            !user.isLocked()
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
