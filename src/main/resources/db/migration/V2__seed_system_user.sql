-- ========================================================================
-- SYSTEM USER (OMNIFY_SYSTEM)
-- Dai dien cho hanh dong tu dong cua he thong (khong phai human), dung lam
-- gia tri created_by/updated_by/assigned_by khi khong co ai dang nhap thuc hien
-- (vi du: tu dang ky tai khoan, gan role mac dinh, job nen...)
-- ========================================================================
ALTER TABLE users
    ADD COLUMN is_system BOOLEAN NOT NULL DEFAULT FALSE;

-- password_hash la bcrypt hop le ve mat dinh dang nhung khong tuong ung mat khau
-- thuc te nao; tai khoan nay con bi chan dang nhap o tang application (is_system = true)
INSERT INTO users (id, email, password_hash, status, email_verified, is_system, created_by, updated_by)
VALUES (
           '00000000-0000-0000-0000-000000000000',
           'system@omnify.internal',
           '$2a$12$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0',
           'ACTIVE',
           true,
           true,
           '00000000-0000-0000-0000-000000000000',
           '00000000-0000-0000-0000-000000000000'
       );

-- Cap nhat lai created_by/updated_by cho 2 role da seed o V1 (truoc do bi NULL)
UPDATE roles
SET created_by = '00000000-0000-0000-0000-000000000000',
    updated_by = '00000000-0000-0000-0000-000000000000'
WHERE name IN ('owner', 'admin');
