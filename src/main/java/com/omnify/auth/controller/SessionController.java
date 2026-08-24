package com.omnify.auth.controller;

import com.omnify.auth.dto.request.RevokeOtherSessionsRequest;
import com.omnify.auth.dto.response.SessionResponse;
import com.omnify.auth.service.SessionService;
import com.omnify.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
        summary = "List active sessions",
        description = "Returns a list of active login sessions associated with the authenticated user."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Active session list retrieved successfully.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                        {
                             "success": true,
                             "message": "Danh sách thiết bị đang đăng nhập",
                             "data": [
                                 {
                                     "sessionId": "a6dd558a-389d-44ed-a8d1-c95206d2330b",
                                     "deviceName": "PostmanRuntime",
                                     "ipAddress": "::1",
                                     "lastUsedAt": null,
                                     "createdAt": "2026-08-14T03:27:03.224959Z",
                                     "current": false
                                 },
                                 {
                                     "sessionId": "d1597bf6-24ef-4a0c-9b87-8c388ccf61a6",
                                     "deviceName": "PostmanRuntime",
                                     "ipAddress": "::1",
                                     "lastUsedAt": null,
                                     "createdAt": "2026-08-14T04:01:17.868005Z",
                                     "current": true
                                 }
                             ],
                             "errorCode": null,
                             "timestamp": "2026-08-14T04:01:21.153418400Z"
                         }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "REFRESH_TOKEN_INVALID - The token is missing or invalid/expired",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "INTERNAL_ERROR - An internal server error occurred. Please try again later.",
            content = @Content
        )
    })
    @GetMapping
    public ResponseEntity<com.omnify.common.response.ApiResponse<List<SessionResponse>>> listSessions(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @RequestParam(required = false) UUID currentSessionId
    ) {
        List<SessionResponse> sessions = sessionService.listActiveSessions(principal.userId(), currentSessionId);
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(sessions, "Danh sách thiết bị đang đăng nhập"));
    }

    @Operation(
        summary = "Log out from a specific device",
        description = "Revokes the specified login session and logs the user out from that device."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "successfully logged out from the selected device.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                        {
                              "success": true,
                              "message": "Đã đăng xuất khỏi thiết bị",
                              "data": null,
                              "errorCode": null,
                              "timestamp": "2026-08-14T04:02:52.749477500Z"
                          }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "REFRESH_TOKEN_INVALID - The token is missing or invalid/expired.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "SESSION_NOT_FOUND - The session could not be found or has already been revoked.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "INTERNAL_ERROR - An internal server error occured. Please try again later.",
            content = @Content
        )

    })
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> revokeSession(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @PathVariable UUID sessionId
    ) {
        sessionService.revokeSession(sessionId, principal.userId());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Đã đăng xuất khỏi thiết bị"));
    }

    @Operation(
        summary = "Log out from all other devices",
        description = "Revoke all active login sessions except the specified current session."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully logged out from all other devices.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                        {
                              "success": true,
                               "message": "Đã đăng xuất khỏi tất cả thiết bị khác",
                               "data": null,
                               "errorCode": null,
                               "timestamp": "2026-08-14T04:04:13.493185100Z"
                           }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "VALIDATION_ERROR - The request data is invalid.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "SESSION_NOT_FOUND - The current session could not be found.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "INTERNAL_ERROR - An internal server error occurred. Please try again later.",
            content = @Content
        )
    })
    @PostMapping("/revoke-others")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> revokeOtherSessions(
        @AuthenticationPrincipal AuthenticatedUser principal,
        @Valid @RequestBody RevokeOtherSessionsRequest request
    ) {
        sessionService.revokeAllOtherSessions(principal.userId(), request.getCurrentSessionId());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Đã đăng xuất khỏi tất cả thiết bị khác"));
    }
}