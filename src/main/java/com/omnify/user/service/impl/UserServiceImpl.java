package com.omnify.user.service.impl;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omnify.common.exception.BusinessException;
import com.omnify.common.exception.ErrorCode;
import com.omnify.user.domain.entity.User;
import com.omnify.user.domain.entity.UserProfile;
import com.omnify.user.domain.repository.UserProfileRepository;
import com.omnify.user.domain.repository.UserRepository;
import com.omnify.user.dto.request.ChangePasswordRequest;
import com.omnify.user.dto.request.UpdateProfileRequest;
import com.omnify.user.dto.response.UserProfileResponse;
import com.omnify.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultProfile(user));

        return toResponse(user, profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultProfile(user));

        String displayName = (request.displayName() == null || request.displayName().isBlank())
            ? request.firstName().trim() + " " + request.lastName().trim()
            : request.displayName().trim();

        profile.setFirstName(request.firstName().trim());
        profile.setLastName(request.lastName().trim());
        profile.setDisplayName(displayName);
        profile.setAvatarUrl(request.avatarUrl());
        profile.setCoverUrl(request.coverUrl());
        profile.setGender(request.gender());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setBio(request.bio());
        profile.setCompanyName(request.companyName());
        profile.setTaxCode(request.taxCode());
        profile.setAddressLine(request.addressLine());
        profile.setCity(request.city());
        profile.setCountryCode(request.countryCode());

        userRepository.save(user);
        UserProfile savedProfile = userProfileRepository.save(profile);
        return toResponse(user, savedProfile);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.SAME_PASSWORD);
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private UserProfile createDefaultProfile(User user) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setUserId(user.getId());
        profile.setFirstName("User");
        profile.setLastName("");
        profile.setDisplayName(user.getEmail());
        return profile;
    }

    private UserProfileResponse toResponse(User user, UserProfile profile) {
        return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getPhone(),
            profile.getFirstName(),
            profile.getLastName(),
            profile.getDisplayName(),
            profile.getAvatarUrl(),
            profile.getCoverUrl(),
            profile.getGender(),
            profile.getDateOfBirth(),
            profile.getBio(),
            profile.getCompanyName(),
            profile.getTaxCode(),
            profile.getAddressLine(),
            profile.getCity(),
            profile.getCountryCode()
        );
    }
}
