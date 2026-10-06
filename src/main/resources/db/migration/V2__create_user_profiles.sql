CREATE TYPE user_gender AS ENUM (
    'MALE',
    'FEMALE',
    'OTHER',
    'UNSPECIFIED'
);

CREATE TABLE user_profiles (
    user_id         UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    display_name    VARCHAR(255),
    avatar_url      TEXT,
    cover_url       TEXT,
    gender          user_gender NOT NULL DEFAULT 'UNSPECIFIED',
    date_of_birth   DATE,
    bio             TEXT,
    company_name    VARCHAR(255),
    tax_code        VARCHAR(50),
    address_line    TEXT,
    city            VARCHAR(100),
    country_code    VARCHAR(2) NOT NULL DEFAULT 'VN',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID REFERENCES users (id) ON DELETE SET NULL,
    updated_by      UUID REFERENCES users (id) ON DELETE SET NULL,
    version         BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE users
    DROP COLUMN full_name,
    DROP COLUMN avatar_url,
    DROP COLUMN locale,
    DROP COLUMN timezone;
