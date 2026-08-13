CREATE TABLE product_inventory (
    id UUID PRIMARY KEY,
    product_id VARCHAR(64) NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    size VARCHAR(32) NOT NULL,
    color VARCHAR(64) NOT NULL,
    stock_on_hand INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    CONSTRAINT ck_product_inventory_stock_nonnegative CHECK (stock_on_hand >= 0),
    CONSTRAINT ck_product_inventory_reserved_nonnegative CHECK (reserved_quantity >= 0),
    CONSTRAINT ck_product_inventory_reserved_within_stock CHECK (reserved_quantity <= stock_on_hand),
    CONSTRAINT uq_product_inventory_variant UNIQUE (product_id, size, color)
);

CREATE INDEX idx_product_inventory_product_id ON product_inventory (product_id);
