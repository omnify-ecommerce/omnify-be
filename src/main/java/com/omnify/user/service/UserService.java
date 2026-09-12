package com.omnify.user.service;

import java.util.UUID;

import com.omnify.user.dto.request.ChangePasswordRequest;
import com.omnify.user.dto.request.UpdateProfileRequest;
import com.omnify.user.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getProfile(UUID userId);

    UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    void changePassword(UUID userId, ChangePasswordRequest request);
}
