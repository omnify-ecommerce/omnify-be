package com.omnify.auth.service;

import com.omnify.auth.domain.entity.RefreshToken;
import com.omnify.auth.domain.repository.RefreshTokenRepository;
import com.omnify.auth.dto.response.SessionResponse;
import com.omnify.common.exception.BusinessException;
import com.omnify.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SessionServiceImpl implements SessionService {

    private final RefreshTokenRepository refreshTokenRepository;

    public SessionServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> listActiveSessions(UUID userId, UUID currentSessionId) {
        List<RefreshToken> sessions =
                refreshTokenRepository.findActiveSessionsByUserId(userId, RefreshToken.Status.VALID);
        return sessions.stream()
                .map(rt -> SessionResponse.builder()
                        .sessionId(rt.getId())
                        .deviceName(rt.getDeviceName())
                        .ipAddress(rt.getIpAddress())
                        .lastUsedAt(rt.getLastUsedAt())
                        .createdAt(rt.getCreatedAt())
                        .current(rt.getId().equals(currentSessionId))
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void revokeSession(UUID sessionId, UUID userId) {
        int affected = refreshTokenRepository.revokeSession(
                sessionId, userId, RefreshToken.Status.VALID, RefreshToken.Status.REVOKED);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void revokeAllOtherSessions(UUID userId, UUID currentSessionId) {
        boolean belongsToUser = refreshTokenRepository.existsByIdAndUserId(currentSessionId, userId);
        if (!belongsToUser) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }

        refreshTokenRepository.revokeAllOtherSessions(
                userId, currentSessionId, RefreshToken.Status.VALID, RefreshToken.Status.REVOKED);
    }
}