package com.omnify.user.service;

import java.util.UUID;

public interface UserService {

    void changePassword(UUID userId, String currentPassword, String newPassword);
}
