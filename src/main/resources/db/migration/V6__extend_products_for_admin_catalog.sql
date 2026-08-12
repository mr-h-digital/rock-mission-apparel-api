ALTER TABLE products
    ADD COLUMN image_url VARCHAR(1024),
    ADD COLUMN blurb TEXT,
    ADD COLUMN art VARCHAR(64),
    ADD COLUMN word VARCHAR(128),
    ADD COLUMN sizes_csv TEXT,
    ADD COLUMN colors_csv TEXT;
