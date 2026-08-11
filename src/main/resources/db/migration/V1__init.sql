CREATE TABLE products (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(64) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    status VARCHAR(32) NOT NULL,
    first_name VARCHAR(128) NOT NULL,
    last_name VARCHAR(128) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(64) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(128) NOT NULL,
    province VARCHAR(64) NOT NULL,
    postal_code VARCHAR(16) NOT NULL,
    country VARCHAR(64) NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    payfast_payment_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders (id),
    product_id VARCHAR(64) NOT NULL REFERENCES products (id),
    product_name VARCHAR(255) NOT NULL,
    size VARCHAR(32) NOT NULL,
    color VARCHAR(64) NOT NULL,
    qty INT NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_orders_payfast_payment_id ON orders (payfast_payment_id);
