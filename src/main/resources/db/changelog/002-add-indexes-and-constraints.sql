--liquibase formatted sql
--changeset cosmocats:002-add-indexes-and-constraints
CREATE UNIQUE INDEX uq_category_name ON category(category_name);
CREATE UNIQUE INDEX uq_product_category_name ON product(category_id, product_name);
CREATE INDEX idx_product_name ON product(product_name);
CREATE INDEX idx_product_category_id ON product(category_id);
CREATE INDEX idx_order_item_order_id ON order_item(order_id);
CREATE INDEX idx_order_item_product_id ON order_item(product_id);
