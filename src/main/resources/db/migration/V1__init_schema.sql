-- backend/src/main/resources/db/migration/V1__init_schema.sql
-- =====================================================================
-- Omnify OMS/ERP — Full Clean Schema (Multi-tenant)
-- PostgreSQL DDL
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto"; -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "citext";   -- case-insensitive email

-- ---------------------------------------------------------------------
-- Enums
-- ---------------------------------------------------------------------
CREATE TYPE company_status AS ENUM ('ACTIVE', 'SUSPENDED', 'PENDING');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING');
CREATE TYPE marketplace_type AS ENUM ('SHOPEE', 'LAZADA', 'TIKTOK_SHOP');
CREATE TYPE token_status AS ENUM ('VALID', 'EXPIRED', 'REVOKED');
CREATE TYPE verification_type AS ENUM ('EMAIL_VERIFY', 'PHONE_VERIFY', 'PASSWORD_RESET');
CREATE TYPE mfa_type AS ENUM ('TOTP', 'SMS', 'EMAIL');
CREATE TYPE mfa_channel AS ENUM ('SMS', 'EMAIL');

-- =====================================================================
-- Table: companies (tenant)
-- =====================================================================
CREATE TABLE companies (
                           id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           name          VARCHAR(255) NOT NULL,
                           status        company_status NOT NULL DEFAULT 'ACTIVE',
                           created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                           updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =====================================================================
-- Table: users
-- company_id NULL  = platform admin (Omnify staff, quản lý toàn bộ tenant)
-- company_id NOT NULL = user thuộc 1 company cụ thể (owner hoặc nhân viên được mời)
-- =====================================================================
CREATE TABLE users (
                       id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       company_id             UUID REFERENCES companies (id) ON DELETE CASCADE,
                       email                  CITEXT,
                       phone                  VARCHAR(20),
                       password_hash          VARCHAR(255) NOT NULL,
                       full_name              VARCHAR(255) NOT NULL,
                       avatar_url             TEXT,
                       locale                 VARCHAR(10) DEFAULT 'vi',
                       timezone               VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
                       status                 user_status NOT NULL DEFAULT 'PENDING',
                       email_verified         BOOLEAN NOT NULL DEFAULT false,
                       phone_verified         BOOLEAN NOT NULL DEFAULT false,

                       failed_login_count     INT NOT NULL DEFAULT 0,
                       last_failed_login_at   TIMESTAMPTZ,
                       locked_until           TIMESTAMPTZ,

                       last_login_at          TIMESTAMPTZ,
                       created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
                       deleted_at             TIMESTAMPTZ,

                       CONSTRAINT uq_users_email UNIQUE (email),
                       CONSTRAINT uq_users_phone UNIQUE (phone),
                       CONSTRAINT chk_users_failed_login_count_nonneg CHECK (failed_login_count >= 0),
                       CONSTRAINT chk_users_email_or_phone CHECK (email IS NOT NULL OR phone IS NOT NULL)
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_company ON users (company_id);
CREATE INDEX idx_users_status ON users (status) WHERE deleted_at IS NULL;

COMMENT ON COLUMN users.company_id IS
  'NULL = platform admin (Omnify staff, xem toàn bộ tenant). NOT NULL = user thuộc company tương ứng.';

-- =====================================================================
-- Table: roles
-- Role hiện tại: 'owner' (chủ shop, gán tự động khi tạo company),
-- 'admin' (platform admin, gán thủ công/nội bộ, không qua public register)
-- =====================================================================
CREATE TABLE roles (
                       id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name         VARCHAR(50) NOT NULL,
                       description  TEXT,
                       is_system    BOOLEAN NOT NULL DEFAULT false,
                       created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                       CONSTRAINT uq_roles_name UNIQUE (name)
);

-- =====================================================================
-- Table: permissions
-- =====================================================================
CREATE TABLE permissions (
                             id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             code         VARCHAR(100) NOT NULL,
                             resource     VARCHAR(50) NOT NULL,
                             action       VARCHAR(50) NOT NULL,
                             description  TEXT,
                             created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                             CONSTRAINT uq_permissions_code UNIQUE (code),
                             CONSTRAINT uq_permissions_resource_action UNIQUE (resource, action)
);

-- =====================================================================
-- Table: role_permissions (N:N)
-- =====================================================================
CREATE TABLE role_permissions (
                                  role_id        UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
                                  permission_id  UUID NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
                                  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id)
);
CREATE INDEX idx_role_permissions_permission ON role_permissions (permission_id);

-- =====================================================================
-- Table: user_roles (N:N)
-- =====================================================================
CREATE TABLE user_roles (
                            user_id      UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                            role_id      UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
                            assigned_by  UUID REFERENCES users (id) ON DELETE SET NULL,
                            assigned_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                            CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id)
);
CREATE INDEX idx_user_roles_role ON user_roles (role_id);

-- =====================================================================
-- Table: refresh_tokens (quản lý phiên đăng nhập / device list)
-- =====================================================================
CREATE TABLE refresh_tokens (
                                id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id        UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                                token_hash     VARCHAR(255) NOT NULL,
                                status         token_status NOT NULL DEFAULT 'VALID',

                                device_name    VARCHAR(255),
                                device_type    VARCHAR(50),
                                user_agent     TEXT,
                                ip_address     VARCHAR(45),
                                last_used_at   TIMESTAMPTZ,

                                expires_at     TIMESTAMPTZ NOT NULL,
                                created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                                revoked_at     TIMESTAMPTZ
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_user_status ON refresh_tokens (user_id, status);
CREATE UNIQUE INDEX idx_refresh_tokens_hash ON refresh_tokens (token_hash);

-- =====================================================================
-- Table: verification_tokens (email/phone verify, password reset)
-- =====================================================================
CREATE TABLE verification_tokens (
                                     id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     user_id      UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                                     token_hash   VARCHAR(255) NOT NULL,
                                     type         verification_type NOT NULL,
                                     expires_at   TIMESTAMPTZ NOT NULL,
                                     used_at      TIMESTAMPTZ,
                                     created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX idx_verification_tokens_hash ON verification_tokens (token_hash);
CREATE INDEX idx_verification_tokens_user ON verification_tokens (user_id);

-- =====================================================================
-- Table: login_attempts (chống brute-force, lịch sử đăng nhập)
-- =====================================================================
CREATE TABLE login_attempts (
                                id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id      UUID REFERENCES users (id) ON DELETE SET NULL,
                                identifier   VARCHAR(255) NOT NULL,
                                success      BOOLEAN NOT NULL,
                                ip_address   VARCHAR(45) NOT NULL,
                                user_agent   TEXT,
                                created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_login_attempts_identifier ON login_attempts (identifier, created_at DESC);
CREATE INDEX idx_login_attempts_ip ON login_attempts (ip_address, created_at DESC);

-- =====================================================================
-- Table: mfa_methods
-- =====================================================================
CREATE TABLE mfa_methods (
                             id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             user_id            UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                             type               mfa_type NOT NULL,
                             secret_encrypted   TEXT,
                             is_verified        BOOLEAN NOT NULL DEFAULT false,
                             is_enabled         BOOLEAN NOT NULL DEFAULT false,
                             created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_mfa_methods_user ON mfa_methods (user_id);
CREATE UNIQUE INDEX uq_mfa_methods_user_type ON mfa_methods (user_id, type);

-- =====================================================================
-- Table: mfa_backup_codes
-- =====================================================================
CREATE TABLE mfa_backup_codes (
                                  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  user_id      UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                                  code_hash    VARCHAR(255) NOT NULL,
                                  used_at      TIMESTAMPTZ,
                                  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_mfa_backup_codes_user ON mfa_backup_codes (user_id);

-- =====================================================================
-- Table: mfa_otp_codes (OTP tạm thời SMS/Email)
-- =====================================================================
CREATE TABLE mfa_otp_codes (
                               id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id      UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                               code_hash    VARCHAR(255) NOT NULL,
                               channel      mfa_channel NOT NULL,
                               expires_at   TIMESTAMPTZ NOT NULL,
                               used_at      TIMESTAMPTZ,
                               created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_mfa_otp_codes_user_time ON mfa_otp_codes (user_id, created_at DESC);

-- =====================================================================
-- Table: marketplace_shops
-- Shop kết nối Shopee/Lazada/TikTok là TÀI SẢN CỦA COMPANY, không phải
-- của riêng user nào -> gắn company_id, giữ connected_by để audit ai connect.
-- =====================================================================
CREATE TABLE marketplace_shops (
                                   id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   company_id         UUID NOT NULL REFERENCES companies (id) ON DELETE CASCADE,
                                   connected_by       UUID REFERENCES users (id) ON DELETE SET NULL,
                                   marketplace        marketplace_type NOT NULL,
                                   shop_id            VARCHAR(100) NOT NULL,
                                   shop_name          VARCHAR(255),
                                   access_token       TEXT,
                                   refresh_token      TEXT,
                                   token_expires_at   TIMESTAMPTZ,
                                   is_active          BOOLEAN NOT NULL DEFAULT true,
                                   connected_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
                                   created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                                   updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
                                   CONSTRAINT uq_marketplace_shops_marketplace_shop UNIQUE (marketplace, shop_id)
);
CREATE INDEX idx_marketplace_shops_company ON marketplace_shops (company_id);

-- =====================================================================
-- Table: audit_logs
-- =====================================================================
CREATE TABLE audit_logs (
                            id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id      UUID REFERENCES users (id) ON DELETE SET NULL,
                            company_id   UUID REFERENCES companies (id) ON DELETE SET NULL,
                            action       VARCHAR(100) NOT NULL,
                            resource     VARCHAR(50),
                            metadata     JSONB,
                            ip_address   INET,
                            created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_logs_user ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_company ON audit_logs (company_id);
CREATE INDEX idx_audit_logs_created ON audit_logs (created_at);

-- =====================================================================
-- Trigger: auto-update updated_at
-- =====================================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_companies_updated_at
    BEFORE UPDATE ON companies
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_roles_updated_at
    BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_marketplace_shops_updated_at
    BEFORE UPDATE ON marketplace_shops
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =====================================================================
-- Seed: system roles
-- =====================================================================
INSERT INTO roles (name, description, is_system) VALUES
                                                     ('owner', 'Chủ shop - toàn quyền quản trị công ty/shop của mình', true),
                                                     ('admin', 'Platform admin (Omnify) - quản lý toàn bộ tenant', true);