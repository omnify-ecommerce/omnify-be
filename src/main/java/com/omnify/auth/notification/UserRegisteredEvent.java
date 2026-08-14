package com.omnify.auth.notification;

import com.omnify.auth.domain.entity.VerificationToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

//class nay quy dinh cac truong yeu cau de gui thong bao khi dang ky
@Getter
@RequiredArgsConstructor
public class UserRegisteredEvent {

    private final UUID userId;
    private final String fullName;
    private final String email;
    private final String phone;
    private final String rawVerificationToken; //gui raw de email gui dung otp cho user nhap
    private final VerificationToken.Type verificationChannel;
}