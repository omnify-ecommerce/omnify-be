package com.omnify.user.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import com.omnify.user.domain.enums.UserGender;

public record UserProfileResponse(
    UUID userId,
    String email,
    String phone,
    String firstName,
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
) {}
