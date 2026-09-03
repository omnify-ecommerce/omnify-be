package com.omnify.common.constant;

import java.util.UUID;

// UUID co dinh cua user he thong (OMNIFY_SYSTEM), phai khop voi seed data trong V2__seed_system_user.sql
public final class SystemUser {

    public static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private SystemUser() {
    }
}
