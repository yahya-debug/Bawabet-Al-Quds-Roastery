-- Bawabet Al-Quds Roastery – Database Schema (Demo Prototype)
-- Student : Yahya Hasan  ID: 1242481
-- Course  : COMP333 – Database Systems  |  Instructor: Yousef Hassouneh

-- ER subset implemented:
--   Entity set 1 : Customer  (+ Individual / Business ISA subtypes)
--   Entity set 2 : Item      (superclass with item_type discriminator)
--   Relationship : Cart (Customer owns one Cart) +
--                  CartItem (M:N  Cart ↔ Item, resolves the "contains" rel.)

CREATE DATABASE IF NOT EXISTS bawabet_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bawabet_db;

-- ENTITY SET 1 : Customer
-- R15. Customer(customer_id, name, email, password)
CREATE TABLE IF NOT EXISTS Customer (
    customer_id INT          AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL
);

-- R16. Individual(customer_id)  – ISA subtype for retail buyers
CREATE TABLE IF NOT EXISTS Individual (
    customer_id INT PRIMARY KEY,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
);

-- R17. Business(customer_id, reg_number, business_type)  – wholesale / B2B
CREATE TABLE IF NOT EXISTS Business (
    customer_id   INT         PRIMARY KEY,
    reg_number    VARCHAR(50) NOT NULL,
    business_type VARCHAR(50) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
);

-- ENTITY SET 2 : Item
-- R9. Item(item_id, name, price, wholesale_price, item_type)
--     item_type ∈ {'coffee', 'roasts', 'spice', 'package'}
CREATE TABLE IF NOT EXISTS Item (
    item_id         INT           AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150)  NOT NULL,
    price           DECIMAL(10,2) NOT NULL CHECK (price > 0),
    wholesale_price DECIMAL(10,2) NOT NULL CHECK (wholesale_price > 0),
    item_type       ENUM('coffee','roasts','spice','package') NOT NULL
);

-- RELATIONSHIP SET : Cart  (Customer has one Cart – total participation)
-- R19. Cart(cart_id, customer_id)
CREATE TABLE IF NOT EXISTS Cart (
    cart_id     INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL UNIQUE,        -- one cart per customer
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
);

-- R20. CartItem(cart_id, item_id, qty)
--      M:N intersection that resolves the "contains" relationship between Cart and Item
CREATE TABLE IF NOT EXISTS CartItem (
    cart_id INT NOT NULL,
    item_id INT NOT NULL,
    qty     INT NOT NULL CHECK (qty > 0),
    PRIMARY KEY (cart_id, item_id),
    FOREIGN KEY (cart_id) REFERENCES Cart(cart_id)  ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES Item(item_id)
);
