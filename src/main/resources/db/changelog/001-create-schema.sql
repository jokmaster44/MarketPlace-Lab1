--liquibase formatted sql
--changeset cosmocats:001-create-schema
CREATE SEQUENCE category_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE product_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE customer_order_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE order_item_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE category (
    id BIGINT PRIMARY KEY DEFAULT nextval('category_id_seq'),
    category_name VARCHAR(100) NOT NULL
);

CREATE TABLE product (
    id BIGINT PRIMARY KEY DEFAULT nextval('product_id_seq'),
    category_id BIGINT REFERENCES category(id),
    product_name VARCHAR(100) NOT NULL,
    price NUMERIC(19, 2) NOT NULL CHECK (price >= 0)
);

CREATE TABLE customer_order (
    id BIGINT PRIMARY KEY DEFAULT nextval('customer_order_id_seq'),
    total NUMERIC(19, 2) NOT NULL CHECK (total >= 0)
);

CREATE TABLE order_item (
    id BIGINT PRIMARY KEY DEFAULT nextval('order_item_id_seq'),
    order_id BIGINT NOT NULL REFERENCES customer_order(id),
    product_id BIGINT NOT NULL REFERENCES product(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 2) NOT NULL CHECK (unit_price >= 0)
);
