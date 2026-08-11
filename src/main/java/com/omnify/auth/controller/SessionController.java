package com.omnify.auth.controller;

import com.omnify.auth.dto.request.RevokeOtherSessionsRequest;
import com.omnify.auth.dto.response.SessionResponse;
import com.omnify.auth.service.SessionService;
import com.omnify.common.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Tag(name = "Sessions", description = "Quản lý phiên đăng nhập / thiết bị")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/auth/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // danh sach thiet bi dang nhap
    @Operation(summary = "Danh sách thiết bị đang đăng nhập")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách thiết bị đang đăng nhập",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = SessionResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Thiếu token hoặc token không hợp lệ/hết hạn "
                    + "(xử lý ở JwtAuthenticationFilter/SecurityConfig, không đi qua GlobalExceptionHandler)"),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "INTERNAL_ERROR",
                                    value = "{\"success\":false,\"message\":\"Lỗi hệ thống, vui lòng thử lại sau\",\"errorCode\":\"INTERNAL_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")))
    })
    @GetMapping
    public ResponseEntity<com.omnify.common.response.ApiResponse<List<SessionResponse>>> listSessions(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Parameter(description = "sessionId của phiên đang gọi API này, để đánh dấu current=true")
            @RequestParam(required = false) UUID currentSessionId) {
        List<SessionResponse> sessions = sessionService.listActiveSessions(principal.userId(), currentSessionId);
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(sessions, "Danh sách thiết bị đang đăng nhập"));
    }

    // dang xuat khoi 1 thiet bi
    @Operation(summary = "Đăng xuất khỏi 1 thiết bị cụ thể")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đã đăng xuất khỏi thiết bị"),
            @ApiResponse(responseCode = "401", description = "Thiếu token hoặc token không hợp lệ/hết hạn "
                    + "(xử lý ở JwtAuthenticationFilter/SecurityConfig, không đi qua GlobalExceptionHandler)"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phiên đăng nhập hoặc phiên không thuộc về user hiện tại",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "SESSION_NOT_FOUND",
                                    value = "{\"success\":false,\"message\":\"Không tìm thấy phiên đăng nhập hoặc đã đăng xuất\",\"errorCode\":\"SESSION_NOT_FOUND\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "INTERNAL_ERROR",
                                    value = "{\"success\":false,\"message\":\"Lỗi hệ thống, vui lòng thử lại sau\",\"errorCode\":\"INTERNAL_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")))
    })
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> revokeSession(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID sessionId) {
        sessionService.revokeSession(sessionId, principal.userId());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Đã đăng xuất khỏi thiết bị"));
    }

    // dnag xuat all thiet bi khac
    @Operation(
            summary = "Đăng xuất tất cả thiết bị khác",
            description = "Giữ lại đúng session có currentSessionId, thu hồi toàn bộ session khác của user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đã đăng xuất khỏi tất cả thiết bị khác"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "VALIDATION_ERROR",
                                    value = "{\"success\":false,\"message\":\"currentSessionId không được để trống\",\"errorCode\":\"VALIDATION_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "401", description = "Thiếu token hoặc token không hợp lệ/hết hạn "
                    + "(xử lý ở JwtAuthenticationFilter/SecurityConfig, không đi qua GlobalExceptionHandler)"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phiên hiện tại (currentSessionId không hợp lệ)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "SESSION_NOT_FOUND",
                                    value = "{\"success\":false,\"message\":\"Không tìm thấy phiên đăng nhập hoặc đã đăng xuất\",\"errorCode\":\"SESSION_NOT_FOUND\",\"timestamp\":\"2026-08-10T10:15:30Z\"}"))),
            @ApiResponse(responseCode = "500", description = "Lỗi hệ thống",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(name = "INTERNAL_ERROR",
                                    value = "{\"success\":false,\"message\":\"Lỗi hệ thống, vui lòng thử lại sau\",\"errorCode\":\"INTERNAL_ERROR\",\"timestamp\":\"2026-08-10T10:15:30Z\"}")))
    })
    @PostMapping("/revoke-others")
    public ResponseEntity<com.omnify.common.response.ApiResponse<Void>> revokeOtherSessions(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody RevokeOtherSessionsRequest request) {
        sessionService.revokeAllOtherSessions(principal.userId(), request.getCurrentSessionId());
        return ResponseEntity.ok(com.omnify.common.response.ApiResponse.success(null, "Đã đăng xuất khỏi tất cả thiết bị khác"));
    }
}