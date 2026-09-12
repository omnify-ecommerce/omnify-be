package com.omnify.auth.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omnify.auth.dto.request.LoginRequest;
import com.omnify.auth.dto.response.LoginResponse;
import com.omnify.auth.service.AuthService;
import com.omnify.common.exception.BusinessException;
import com.omnify.common.exception.ErrorCode;
import com.omnify.security.CustomUserDetails;
import com.omnify.security.JwtTokenProvider;
import com.omnify.user.domain.entity.User;
import com.omnify.user.domain.enums.UserStatus;
import com.omnify.user.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (user.getStatus() == UserStatus.PENDING) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }

        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        if (user.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        CustomUserDetails details = CustomUserDetails.fromUser(user);
        String token = jwtTokenProvider.generateToken(details);
        user.registerSuccessfulLogin();
        userRepository.save(user);

        return new LoginResponse(token, "Bearer", jwtTokenProvider.getExpirationMs(), user.getId(), user.getEmail());
    }
}
