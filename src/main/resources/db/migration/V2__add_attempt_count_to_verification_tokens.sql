ALTER TABLE verification_tokens
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0;
INSERT INTO roles (name, description, is_system) VALUES
                                                     ('owner', 'Chủ shop - toàn quyền quản trị công ty/shop của mình', true),
                                                     ('admin', 'Platform admin (Omnify) - quản lý toàn bộ tenant', true);