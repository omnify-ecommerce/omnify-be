-- ========================================================================
-- EXTENSIONS
-- ========================================================================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "citext";
CREATE EXTENSION IF NOT EXISTS "moddatetime";

-- ========================================================================
-- ENUMS
-- ========================================================================
CREATE TYPE user_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED',
    'PENDING'
);

CREATE TYPE marketplace_type AS ENUM (
    'SHOPEE',
    'LAZADA',
    'TIKTOK_SHOP'
);

CREATE TYPE token_status AS ENUM (
    'VALID',
    'EXPIRED',
    'REVOKED'
);

CREATE TYPE verification_type AS ENUM (
    'EMAIL_VERIFICATION',
    'EMAIL_CHANGE',
    'PHONE_VERIFICATION',
    'PHONE_CHANGE',
    'PASSWORD_RESET'
);

CREATE TYPE login_failure_reason AS ENUM (
    'INVALID_CREDENTIALS',
    'ACCOUNT_NOT_FOUND',
    'ACCOUNT_LOCKED',
    'ACCOUNT_DISABLED',
    'EMAIL_NOT_VERIFIED',
    'PHONE_NOT_VERIFIED',
    'MFA_FAILED'
);

CREATE TYPE mfa_type AS ENUM (
    'TOTP',
    'SMS',
    'EMAIL'
);

CREATE TYPE mfa_channel AS ENUM (
    'SMS',
    'EMAIL'
);

CREATE TYPE mfa_purpose AS ENUM (
    'LOGIN',
    'VERIFY_TRANSACTION',
    'ENABLE_MFA',
    'DISABLE_MFA',
    'DELETE_ACCOUNT'
);

-- ========================================================================
-- TABLE: USERS
-- ========================================================================
CREATE TABLE users (
                       id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email                   CITEXT NOT NULL,
                       phone                   VARCHAR(20),
                       password_hash           VARCHAR(255) NOT NULL,
                       full_name               VARCHAR(255) NOT NULL,
                       avatar_url              TEXT,
                       locale                  VARCHAR(10) NOT NULL DEFAULT 'vi-VN',
                       timezone                VARCHAR(50) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
                       status                  user_status NOT NULL DEFAULT 'PENDING',
                       email_verified          BOOLEAN NOT NULL DEFAULT FALSE,
                       phone_verified          BOOLEAN NOT NULL DEFAULT FALSE,
                       failed_login_count      INTEGER NOT NULL DEFAULT 0,
                       last_failed_login_at    TIMESTAMPTZ,
                       locked_until            TIMESTAMPTZ,
                       last_login_at           TIMESTAMPTZ,
                       created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       deleted_at              TIMESTAMPTZ,
                       CONSTRAINT chk_users_failed_login_count CHECK (failed_login_count >= 0)
);

CREATE UNIQUE INDEX uq_users_email_active ON users (email) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX uq_users_phone_active ON users (phone) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_status ON users (status) WHERE deleted_at IS NULL;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION moddatetime(updated_at);

-- ========================================================================
-- TABLE: ROLES & PERMISSIONS
-- ========================================================================
CREATE TABLE roles (
                       id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name        VARCHAR(50) UNIQUE NOT NULL,
                       description TEXT,
                       is_system   BOOLEAN NOT NULL DEFAULT FALSE,
                       created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_roles_updated_at
    BEFORE UPDATE ON roles
    FOR EACH ROW
    EXECUTE FUNCTION moddatetime(updated_at);

CREATE TABLE permissions (
                             id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             code        VARCHAR(100) UNIQUE NOT NULL,   -- <resource>.<action>
                             resource    VARCHAR(50) NOT NULL,
                             action      VARCHAR(50) NOT NULL,
                             description TEXT,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                             CONSTRAINT uq_permissions_resource_action UNIQUE (resource, action)
);

-- Role <-> Permission (N:N)
CREATE TABLE role_permissions (
                                  role_id         UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
                                  permission_id   UUID NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
                                  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id)
);

-- User <-> Role (N:N)
CREATE TABLE user_roles (
                            user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                            role_id     UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
                            assigned_by UUID REFERENCES users (id) ON DELETE SET NULL,
                            assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id)
);

-- ========================================================================
-- TABLE: REFRESH TOKENS (Sessions/Tokens)
-- ========================================================================
CREATE TABLE refresh_tokens (
                                id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                                token_hash      VARCHAR(255) UNIQUE NOT NULL,
                                status          token_status NOT NULL DEFAULT 'VALID',
                                device_name     VARCHAR(100),
                                user_agent      TEXT,
                                ip_address      INET,
                                expires_at      TIMESTAMPTZ NOT NULL,
                                revoked_at      TIMESTAMPTZ,
                                last_used_at    TIMESTAMPTZ,
                                created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_user_status ON refresh_tokens (user_id, status);

-- ========================================================================
-- TABLE: VERIFICATION TOKENS (Email/Phone Verification & Password Reset)
-- ========================================================================
CREATE TABLE verification_tokens (
                                     id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                                     token_hash  VARCHAR(255) UNIQUE NOT NULL,
                                     type        verification_type NOT NULL,
                                     expires_at  TIMESTAMPTZ NOT NULL,
                                     used_at     TIMESTAMPTZ,
                                     created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_verification_tokens_user ON verification_tokens (user_id);

-- ========================================================================
-- TABLE: LOGIN ATTEMPTS
-- ========================================================================
CREATE TABLE login_attempts (
                                id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id         UUID REFERENCES users (id) ON DELETE SET NULL,
                                identifier      VARCHAR(255) NOT NULL, -- Email/Phone
                                success         BOOLEAN NOT NULL,
                                failure_reason  login_failure_reason,
                                ip_address      INET NOT NULL,
                                user_agent      TEXT,
                                created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_login_attempts_identifier ON login_attempts (identifier, created_at DESC);
CREATE INDEX idx_login_attempts_ip ON login_attempts (ip_address, created_at DESC);

-- ========================================================================
-- TABLE: MFA METHODS (TOTP/SMS/Email)
-- ========================================================================
CREATE TABLE mfa_methods (
                             id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             user_id             UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                             type                mfa_type NOT NULL,
                             destination         VARCHAR(255),   -- SMS/Email
                             secret_encrypted    TEXT,           -- TOTP
                             is_verified         BOOLEAN NOT NULL DEFAULT FALSE,
                             is_enabled          BOOLEAN NOT NULL DEFAULT FALSE,
                             is_primary          BOOLEAN NOT NULL DEFAULT FALSE,
                             created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                             CONSTRAINT uq_mfa_methods_user_type UNIQUE (user_id, type),
                             CONSTRAINT chk_totp_secret CHECK (
                                 type != 'TOTP' OR (secret_encrypted IS NOT NULL AND destination IS NULL)
),
    CONSTRAINT chk_channel_destination CHECK (
        type = 'TOTP' OR (destination IS NOT NULL AND secret_encrypted IS NULL)
    )
);

CREATE INDEX idx_mfa_methods_user ON mfa_methods (user_id);
CREATE UNIQUE INDEX uq_mfa_methods_user_primary ON mfa_methods (user_id) WHERE is_primary;

-- ========================================================================
-- TABLE: MFA BACKUP CODES
-- ========================================================================
CREATE TABLE mfa_backup_codes (
                                  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                                  code_hash   VARCHAR(255) NOT NULL,
                                  used_at     TIMESTAMPTZ,
                                  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mfa_backup_codes_user ON mfa_backup_codes (user_id);

-- ========================================================================
-- TABLE: MFA OTP CODES
-- ========================================================================
CREATE TABLE mfa_otp_codes (
                               id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id         UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                               mfa_method_id   UUID REFERENCES mfa_methods (id) ON DELETE SET NULL,
                               code_hash       VARCHAR(255) NOT NULL,
                               channel         mfa_channel NOT NULL,
                               destination     VARCHAR(255),
                               purpose         mfa_purpose NOT NULL,
                               attempts        INTEGER NOT NULL DEFAULT 0,
                               max_attempts    INTEGER NOT NULL DEFAULT 5,
                               expires_at      TIMESTAMPTZ NOT NULL,
                               used_at         TIMESTAMPTZ,
                               created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                               CONSTRAINT chk_mfa_otp_codes_attempts CHECK (attempts >= 0),
                               CONSTRAINT chk_mfa_otp_codes_max_attempts CHECK (max_attempts > 0)
);

CREATE INDEX idx_mfa_otp_codes_user_time ON mfa_otp_codes (user_id, created_at DESC);

-- ========================================================================
-- TABLE: MARKETPLACE INTEGRATION (Shopee/Lazada/TikTok Shop)
-- ========================================================================
CREATE TABLE marketplace_shops (
                                   id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   user_id                 UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                                   marketplace             marketplace_type NOT NULL,
                                   shop_id                 VARCHAR(100) NOT NULL,
                                   shop_name               VARCHAR(255),
                                   access_token_encrypted  TEXT,
                                   refresh_token_encrypted TEXT,
                                   token_expires_at        TIMESTAMPTZ,
                                   is_active               BOOLEAN NOT NULL DEFAULT TRUE,
                                   last_synced_at          TIMESTAMPTZ,
                                   connected_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                   disconnected_at         TIMESTAMPTZ,
                                   created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                   updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                   CONSTRAINT uq_marketplace_shop UNIQUE (marketplace, shop_id)
);

CREATE INDEX idx_marketplace_shops_user ON marketplace_shops (user_id);

CREATE TRIGGER trg_marketplace_shops_updated_at
    BEFORE UPDATE ON marketplace_shops
    FOR EACH ROW
    EXECUTE FUNCTION moddatetime(updated_at);

-- ========================================================================
-- TABLE: AUDIT LOGS
-- ========================================================================
CREATE TABLE audit_logs (
                            id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id     UUID REFERENCES users (id) ON DELETE SET NULL,
                            action      VARCHAR(100) NOT NULL,
                            resource    VARCHAR(50),
                            metadata    JSONB,
                            ip_address  INET,
                            created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_created ON audit_logs (created_at);
