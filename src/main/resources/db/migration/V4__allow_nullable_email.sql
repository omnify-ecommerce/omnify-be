ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
ALTER TABLE users ADD CONSTRAINT chk_users_email_or_phone
    CHECK (email IS NOT NULL OR phone IS NOT NULL);