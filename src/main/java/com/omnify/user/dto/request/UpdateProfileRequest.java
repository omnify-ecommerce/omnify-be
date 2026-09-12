package com.omnify.user.dto.request;

import java.time.LocalDate;

import com.omnify.user.domain.enums.UserGender;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update the current user's profile")
public record UpdateProfileRequest(
    @NotBlank(message = "Tên không được để trống")
    String firstName,

    @NotBlank(message = "Họ không được để trống")
    String lastName,

    String displayName,
    String avatarUrl,
    String coverUrl,
    UserGender gender,
    LocalDate dateOfBirth,
    String bio,
    String companyName,
    String taxCode,
    String addressLine,
    String city,
    String countryCode
) {
    
    public UpdateProfileRequest {
        if (gender == null) {
            gender = UserGender.UNSPECIFIED;
        }

        if (countryCode == null || countryCode.isBlank()) {
            countryCode = "VN";
        }
    }
}
