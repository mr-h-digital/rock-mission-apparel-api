ALTER TABLE app_users
    ADD COLUMN phone VARCHAR(64),
    ADD COLUMN address_line1 VARCHAR(255),
    ADD COLUMN address_line2 VARCHAR(255),
    ADD COLUMN city VARCHAR(128),
    ADD COLUMN province VARCHAR(64),
    ADD COLUMN postal_code VARCHAR(16),
    ADD COLUMN country VARCHAR(64);
