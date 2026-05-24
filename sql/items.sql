-- ═══════════════════════════════════════════════════════════════════════════
-- Bawabet Al-Quds Roastery – Item Tables & Test Data
-- Student : Mohammed Quraan  ID: 1243436
-- ═══════════════════════════════════════════════════════════════════════════

USE bawabet_db;

-- ── Main Item Table ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Item (
                                    item_id INT AUTO_INCREMENT PRIMARY KEY,
                                    name VARCHAR(150) NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    wholesale_price DECIMAL(10,2) NOT NULL CHECK (wholesale_price > 0),
    item_type ENUM('coffee','roasts','spice','package') NOT NULL
    );

-- ── Coffee Subtype Table ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Coffee (
                                      item_id INT PRIMARY KEY,
                                      beanType VARCHAR(100) NOT NULL,
    lightRatio DECIMAL(5,2) NOT NULL,
    darkRatio DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (item_id) REFERENCES Item(item_id) ON DELETE CASCADE
    );

-- ── Roasts Subtype Table ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Roasts (
                                      item_id INT PRIMARY KEY,
                                      FOREIGN KEY (item_id) REFERENCES Item(item_id) ON DELETE CASCADE
    );

-- ── Spice Subtype Table ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS Spice (
                                     item_id INT PRIMARY KEY,
                                     FOREIGN KEY (item_id) REFERENCES Item(item_id) ON DELETE CASCADE
    );

-- ── Package Subtype Table ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS `Package` (
                                         item_id INT PRIMARY KEY,
                                         package_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    FOREIGN KEY (item_id) REFERENCES Item(item_id) ON DELETE CASCADE
    );

-- ── Insert Coffee Items ──────────────────────────────────────────────────
INSERT INTO Item (name, price, wholesale_price, item_type)
SELECT 'Arabic Coffee', 25.00, 20.00, 'coffee'
    WHERE NOT EXISTS (SELECT 1 FROM Item WHERE name = 'Arabic Coffee');

INSERT INTO Item (name, price, wholesale_price, item_type)
SELECT 'Turkish Coffee', 30.00, 24.00, 'coffee'
    WHERE NOT EXISTS (SELECT 1 FROM Item WHERE name = 'Turkish Coffee');

INSERT INTO Item (name, price, wholesale_price, item_type)
SELECT 'Dark Coffee Blend', 35.00, 28.00, 'coffee'
    WHERE NOT EXISTS (SELECT 1 FROM Item WHERE name = 'Dark Coffee Blend');

-- ── Insert Roasts Items ──────────────────────────────────────────────────
INSERT INTO Item (name, price, wholesale_price, item_type)
SELECT 'Roasted Cashew', 45.00, 38.00, 'roasts'
    WHERE NOT EXISTS (SELECT 1 FROM Item WHERE name = 'Roasted Cashew');

INSERT INTO Item (name, price, wholesale_price, item_type)
SELECT 'Roasted Almonds', 40.00, 33.00, 'roasts'
    WHERE NOT EXISTS (SELECT 1 FROM Item WHERE name = 'Roasted Almonds');

-- ── Insert Spice Items ───────────────────────────────────────────────────
INSERT INTO Item (name, price, wholesale_price, item_type)
SELECT 'Cardamom', 15.00, 10.00, 'spice'
    WHERE NOT EXISTS (SELECT 1 FROM Item WHERE name = 'Cardamom');

INSERT INTO Item (name, price, wholesale_price, item_type)
SELECT 'Cinnamon', 12.00, 8.00, 'spice'
    WHERE NOT EXISTS (SELECT 1 FROM Item WHERE name = 'Cinnamon');

-- ── Insert Package Items ─────────────────────────────────────────────────
INSERT INTO Item (name, price, wholesale_price, item_type)
SELECT 'Family Coffee Package', 90.00, 75.00, 'package'
    WHERE NOT EXISTS (SELECT 1 FROM Item WHERE name = 'Family Coffee Package');

-- ── Insert Coffee Details ────────────────────────────────────────────────
INSERT IGNORE INTO Coffee (item_id, beanType, lightRatio, darkRatio)
SELECT item_id, 'Arabica', 70.00, 30.00
FROM Item
WHERE name = 'Arabic Coffee';

INSERT IGNORE INTO Coffee (item_id, beanType, lightRatio, darkRatio)
SELECT item_id, 'Arabica', 80.00, 20.00
FROM Item
WHERE name = 'Turkish Coffee';

INSERT IGNORE INTO Coffee (item_id, beanType, lightRatio, darkRatio)
SELECT item_id, 'Blend', 40.00, 60.00
FROM Item
WHERE name = 'Dark Coffee Blend';

-- ── Insert Roasts Details ────────────────────────────────────────────────
INSERT IGNORE INTO Roasts (item_id)
SELECT item_id
FROM Item
WHERE name = 'Roasted Cashew';

INSERT IGNORE INTO Roasts (item_id)
SELECT item_id
FROM Item
WHERE name = 'Roasted Almonds';

-- ── Insert Spice Details ─────────────────────────────────────────────────
INSERT IGNORE INTO Spice (item_id)
SELECT item_id
FROM Item
WHERE name = 'Cardamom';

INSERT IGNORE INTO Spice (item_id)
SELECT item_id
FROM Item
WHERE name = 'Cinnamon';

-- ── Insert Package Details ───────────────────────────────────────────────
INSERT IGNORE INTO `Package` (item_id, package_name, description)
SELECT item_id, 'Family Coffee Package',
       'Coffee package with Arabic coffee, Turkish coffee, and cardamom'
FROM Item
WHERE name = 'Family Coffee Package';

-- ── Display All Tables ───────────────────────────────────────────────────
SELECT * FROM Item;
SELECT * FROM Coffee;
SELECT * FROM Roasts;
SELECT * FROM Spice;
SELECT * FROM `Package`;

-- ── Display Coffee Items With Their Details ──────────────────────────────
SELECT
    i.item_id,
    i.name,
    i.price,
    i.wholesale_price,
    i.item_type,
    c.beanType,
    c.lightRatio,
    c.darkRatio
FROM Item i
         JOIN Coffee c ON i.item_id = c.item_id;

-- ── Display Package Items With Their Details ─────────────────────────────
SELECT
    i.item_id,
    i.name,
    i.price,
    i.wholesale_price,
    i.item_type,
    p.package_name,
    p.description
FROM Item i
         JOIN `Package` p ON i.item_id = p.item_id;