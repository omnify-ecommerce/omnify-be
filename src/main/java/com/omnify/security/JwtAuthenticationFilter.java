package com.omnify.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/* Filter nay se chiu trach nghiem xac thuc JWT va thiet lap danh tinh cua user vào security context cho các bước phía
 sau sử dung. Class nayf chi xac dinh request dang la ai khong quyet dinh request nay co duoc truy cap hay khong
 ma thuc hien boi authentication chay sau de endpoint public van goi duoc binh thuong du khong co token */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        //lay ra token cua request do va kiem tra
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Jws<Claims> jws = jwtTokenProvider.parseToken(token);
                Claims claims = jws.getPayload();
                UUID userId = UUID.fromString(claims.getSubject());
                String role = claims.get("role", String.class);
                //neu dung tao 1 principal dai dien cho user hien tai gom role va userid
                AuthenticatedUser principal = new AuthenticatedUser(userId, role);
                List<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                //danh dau da duoc xac thuc
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException ex) {
                //token sai chu ky hoac het han -> ko set authentication de cac class phia sau quyet dinh xu li
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}