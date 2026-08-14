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
            example = "e2TY7c7SLefyuvUgzQqM0G1O6IUF72JhcsADsBPLpK4")
    private String refreshToken;

}