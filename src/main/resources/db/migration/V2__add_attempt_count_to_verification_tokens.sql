ALTER TABLE verification_tokens
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0;