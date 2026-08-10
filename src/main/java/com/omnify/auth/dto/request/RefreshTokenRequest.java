// backend/src/main/java/com/omnify/auth/dto/request/RefreshTokenRequest.java
package com.omnify.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "RefreshTokenRequest", description = "Request làm mới access token")
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken không được để trống")
    @Schema(description = "Refresh token nhận được lúc đăng nhập",
            example = "e5e5dfe7-4cb8-4ec6-8f33-5f7e8d5d7b2a")
    private String refreshToken;

}