package com.omnify.auth.controller;

import com.omnify.auth.dto.request.RevokeOtherSessionsRequest;
import com.omnify.auth.dto.response.SessionResponse;
import com.omnify.auth.service.SessionService;
import com.omnify.common.response.ApiResponse;
import com.omnify.common.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Sessions", description = "Quản lý phiên đăng nhập / thiết bị")
@RestController
@RequestMapping("/api/v1/auth/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Operation(summary = "Danh sách thiết bị đang đăng nhập")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionResponse>>> listSessions(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Parameter(description = "sessionId của phiên đang gọi API này, để đánh dấu current=true")
            @RequestParam(required = false) UUID currentSessionId) {
        List<SessionResponse> sessions = sessionService.listActiveSessions(principal.userId(), currentSessionId);
        return ResponseEntity.ok(ApiResponse.success(sessions, "Danh sách thiết bị đang đăng nhập"));
    }

    @Operation(summary = "Đăng xuất khỏi 1 thiết bị cụ thể")
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID sessionId) {
        sessionService.revokeSession(sessionId, principal.userId());
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đăng xuất khỏi thiết bị"));
    }

    @Operation(summary = "Đăng xuất tất cả thiết bị khác",
            description = "Giữ lại đúng session có currentSessionId, thu hồi toàn bộ session khác của user.")
    @PostMapping("/revoke-others")
    public ResponseEntity<ApiResponse<Void>> revokeOtherSessions(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody RevokeOtherSessionsRequest request) {
        sessionService.revokeAllOtherSessions(principal.userId(), request.getCurrentSessionId());
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đăng xuất khỏi tất cả thiết bị khác"));
    }
}