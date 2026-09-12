package com.omnify.auth.domain.enums;

public enum LoginFailureReason {
    INVALID_CREDENTIALS,
    ACCOUNT_NOT_FOUND,
    ACCOUNT_LOCKED,
    ACCOUNT_DISABLED,
    EMAIL_NOT_VERIFIED,
    PHONE_NOT_VERIFIED,
    MFA_FAILED
}
