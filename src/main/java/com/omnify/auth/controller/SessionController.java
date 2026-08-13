package com.omnify.auth.controller;

import com.omnify.auth.dto.request.RevokeOtherSessionsRequest;
import com.omnify.auth.dto.response.SessionResponse;
import com.omnify.auth.service.SessionService;
import com.omnify.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/auth/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // danh sach thiet bi dang nhap
    @GetMapping
    public ResponseEntity<com.omnify.common.response.ApiResponse<List<SessionResponse>>> listSessions(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @RequestParam(required = false) UUID currentSessionId) {
        List<SessionResponse> sessions = sessionService.listActiveSessions(principal.userId(), currentSessionId);
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(sessions, "Danh sách thiết bị đang đăng nhập"));
    }

    // dang xuat khoi 1 thiet bi
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> revokeSession(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @PathVariable UUID sessionId) {
        sessionService.revokeSession(sessionId, principal.userId());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Đã đăng xuất khỏi thiết bị"));
    }

    // dnag xuat all thiet bi khac
    @PostMapping("/revoke-others")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> revokeOtherSessions(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @Valid @RequestBody RevokeOtherSessionsRequest request) {
        sessionService.revokeAllOtherSessions(principal.userId(), request.getCurrentSessionId());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Đã đăng xuất khỏi tất cả thiết bị khác"));
    }
}