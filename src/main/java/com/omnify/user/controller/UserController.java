package com.omnify.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.omnify.common.constant.OpenApiConstant;
import com.omnify.common.response.ApiResponse;
import com.omnify.security.CustomUserDetails;
import com.omnify.user.dto.request.ChangePasswordRequest;
import com.omnify.user.dto.request.UpdateProfileRequest;
import com.omnify.user.dto.response.UserProfileResponse;
import com.omnify.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "Get profile",
        description = "Get the current user profile, including ID, email, phone, and other personal information",
        security = @SecurityRequirement(name = OpenApiConstant.SECURITY_SCHEME_NAME)
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentProfile(
        @AuthenticationPrincipal CustomUserDetails details
    ) {
        return ResponseEntity.ok(ApiResponse
            .success(userService.getProfile(details.getUserId()), "Hồ sơ đã được truy xuất thành công"));
    }

    @Operation(
        summary = "Update profile",
        description = "Update the current user profile",
        security = @SecurityRequirement(name = OpenApiConstant.SECURITY_SCHEME_NAME))
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
        @AuthenticationPrincipal CustomUserDetails details,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse
            .success(userService.updateProfile(details.getUserId(), request), "Hồ sơ đã được cập nhật thành công"));
    }

    @Operation(
        summary = "Change password",
        description = "Change the current user's password after verifying the old password",
        security = @SecurityRequirement(name = OpenApiConstant.SECURITY_SCHEME_NAME)
    )
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
        @AuthenticationPrincipal CustomUserDetails details,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(details.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Mật khẩu đã được thay đổi thành công"));
    }
}
