package com.omnify.auth.service.impl;

import com.omnify.security.TokenGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.omnify.auth.domain.entity.VerificationToken;
import com.omnify.auth.domain.enums.VerificationType;
import com.omnify.auth.domain.repository.VerificationTokenRepository;
import com.omnify.auth.dto.request.ForgotPasswordRequest;
import com.omnify.auth.dto.request.ResetPasswordRequest;
import com.omnify.auth.service.PasswordResetService;
import com.omnify.common.exception.BusinessException;
import com.omnify.common.exception.ErrorCode;
import com.omnify.common.notification.EmailService;
import com.omnify.config.properties.FrontendProperties;
import com.omnify.config.properties.TokenProperties;
import com.omnify.security.TokenHasher;
import com.omnify.user.domain.entity.User;
import com.omnify.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final TokenGenerator tokenGenerator;
    private final TokenProperties tokenProperties;
    private final TokenHasher tokenHasher;
    private final FrontendProperties frontendProperties;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override 
    @Transactional 
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.email())
            .ifPresent(user -> {
                List<VerificationToken> tokens = verificationTokenRepository
                    .findByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), VerificationType.PASSWORD_RESET);
                verificationTokenRepository.deleteAll(tokens);

                // Create a new password reset token
                String rawToken = tokenGenerator.generateToken();
                Duration expiration = tokenProperties.passwordReset().expiration();
                VerificationToken token = VerificationToken.builder()
                    .userId(user.getId())
                    .tokenHash(tokenHasher.hash(rawToken))
                    .type(VerificationType.PASSWORD_RESET)
                    .expiresAt(Instant.now().plus(expiration))
                    .build();
                verificationTokenRepository.save(token);

                // Send password reset link via email
                String resetLink = UriComponentsBuilder
                    .fromUriString(frontendProperties.baseUrl())
                    .path("/reset-password")
                    .queryParam("token", rawToken)
                    .toUriString();
                String htmlBody = """
                    <p>We received a request to reset your password.</p>
                    <p>Open this link: <a href="%s">%s</a></p>
                    <p>This link is valid for %s minutes.</p>
                    """.formatted(resetLink, resetLink, expiration.toMinutes());                    
                emailService.sendHtml(user.getEmail(), "Reset Password", htmlBody);
            });
    }

    @Override 
    @Transactional 
    public void resetPassword(ResetPasswordRequest request) {
        String rawToken = request.token();
        List<VerificationToken> tokens = verificationTokenRepository
            .findByTypeAndUsedAtIsNullOrderByCreatedAtDesc(VerificationType.PASSWORD_RESET);
        
        VerificationToken matchedToken = tokens.stream()
            .filter(token -> !token.isExpired())
            .filter(token -> tokenHasher.matches(rawToken, token.getTokenHash()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        User user = userRepository.findById(matchedToken.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.SAME_PASSWORD);
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        matchedToken.markUsed();
        userRepository.save(user);
        verificationTokenRepository.save(matchedToken);
    }
}
