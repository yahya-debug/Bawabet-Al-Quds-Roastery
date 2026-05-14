-- ═══════════════════════════════════════════════════════════════════════════
-- Bawabet Al-Quds Roastery – Test Data
-- Student : Yahya Hasan  ID: 1242481
-- ═══════════════════════════════════════════════════════════════════════════

USE bawabet_db;

-- ── Customers ────────────────────────────────────────────────────────────────
INSERT INTO Customer (name, email, password) VALUES
    ('Ahmad Khalil',   'ahmad@example.com',  'pass123'),
    ('Sara Mansour',   'sara@example.com',   'pass456'),
    ('Demo User',      'demo@bawabet.com',   'demo');

INSERT INTO Individual (customer_id) VALUES (1), (2), (3);

-- ── Items – Coffee ───────────────────────────────────────────────────────────
INSERT INTO Item (name, price, wholesale_price, item_type) VALUES
    ('Brazilian Arabica',      12.50, 10.00, 'coffee'),
    ('Ethiopian Yirgacheffe',  15.00, 12.50, 'coffee'),
    ('Colombian Supremo',      13.00, 11.00, 'coffee'),
    ('Turkish Blend',          11.00,  9.00, 'coffee');

-- ── Items – Roasts ───────────────────────────────────────────────────────────
INSERT INTO Item (name, price, wholesale_price, item_type) VALUES
    ('Roasted Almonds',         8.00,  6.50, 'roasts'),
    ('Roasted Cashews',        10.50,  8.75, 'roasts'),
    ('Roasted Pumpkin Seeds',   7.00,  5.50, 'roasts');

-- ── Items – Spice & Herb ─────────────────────────────────────────────────────
INSERT INTO Item (name, price, wholesale_price, item_type) VALUES
    ('Cardamom Blend',          5.00,  4.00, 'spice'),
    ('Cinnamon Sticks',         4.50,  3.50, 'spice'),
    ('Mixed Herbs Pack',        6.00,  5.00, 'spice');

-- ── Items – Packages ─────────────────────────────────────────────────────────
INSERT INTO Item (name, price, wholesale_price, item_type) VALUES
    ('Morning Starter Pack',   25.00, 20.00, 'package'),
    ('Premium Gift Bundle',    35.00, 28.00, 'package');

-- ── Carts (one per customer) ──────────────────────────────────────────────────
INSERT INTO Cart (customer_id) VALUES (1), (2), (3);

-- ── CartItems ─────────────────────────────────────────────────────────────────
INSERT INTO CartItem (cart_id, item_id, qty) VALUES
    (1, 1, 2),   -- Ahmad:  2× Brazilian Arabica
    (1, 8, 1),   -- Ahmad:  1× Cardamom Blend
    (2, 3, 1),   -- Sara:   1× Colombian Supremo
    (2, 5, 3),   -- Sara:   3× Roasted Almonds
    (3, 11, 1);  -- Demo:   1× Morning Starter Pack
