package com.omnify.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@Getter
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenTtlSeconds;

    // lay cac gia tri da setup ben application.yml
    public JwtTokenProvider(
        @Value("${omnify.security.jwt.secret}") String secret,
        @Value("${omnify.security.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    //tao access token JWT sau khi xac thuc thanh cong. gom role va userId
    public String generateAccessToken(UUID userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(userId.toString())
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(accessTokenTtlSeconds)))
            .signWith(key)
            .compact();
    }

    // kiem tra chu ky va thoi han cua token truyen vao tra loi neu ko hop le hoac het han
    public Jws<Claims> parseToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }

}