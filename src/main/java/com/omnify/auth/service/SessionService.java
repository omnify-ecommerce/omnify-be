package com.omnify.auth.service;

import com.omnify.auth.dto.response.SessionResponse;

import java.util.List;
import java.util.UUID;

public interface SessionService {

    List<SessionResponse> listActiveSessions(UUID userId, UUID currentSessionId);

    void revokeSession(UUID sessionId, UUID userId);

    void revokeAllOtherSessions(UUID userId, UUID currentSessionId);
}