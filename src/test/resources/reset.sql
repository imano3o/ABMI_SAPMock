-- Empties all tables before each test; the test then loads data.sql again.
DELETE FROM material_document_item;
DELETE FROM material_document;
DELETE FROM purchase_order_item;
DELETE FROM purchase_order;
DELETE FROM material_stock;
