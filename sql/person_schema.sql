-- Bawabet Al-Quds Roastery – Person & Connected Entities Schema
-- Student : Yahya Hasan  ID: 1242481
-- Course  : COMP333 – Database Systems  |  Instructor: Yousef Hassouneh

-- ER subset implemented:
--   Supertype      : Person
--   ISA subtypes   : Customer  (→ Individual / Business)
--                    Employee
--                    Admin
--   Supporting     : Location, Branch, Supplier, Item
--   Relationships  : Employee ↔ Item  (manages, M:N)

CREATE DATABASE IF NOT EXISTS bawabet_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bawabet_db;

-- ─────────────────────────────────────────────
-- INDEPENDENT SUPPORTING ENTITIES
-- ─────────────────────────────────────────────

-- Location – shared by Customer and Branch
CREATE TABLE IF NOT EXISTS Location (
    location_id INT          AUTO_INCREMENT PRIMARY KEY,
    street      VARCHAR(100) NOT NULL,
    city        VARCHAR(45)  NOT NULL,
    zip         VARCHAR(45)  NOT NULL
);

-- Branch – physical store location
CREATE TABLE IF NOT EXISTS Branch (
    branch_id   INT         AUTO_INCREMENT PRIMARY KEY,
    branch_name VARCHAR(45) NOT NULL,
    location_id INT         NOT NULL,
    FOREIGN KEY (location_id) REFERENCES Location(location_id)
);

-- Supplier – provides items to the roastery
CREATE TABLE IF NOT EXISTS Supplier (
    supplier_id INT          AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(45)  NOT NULL,
    phone       VARCHAR(45)  NOT NULL
);

-- ─────────────────────────────────────────────
-- SUPERTYPE : Person
-- ─────────────────────────────────────────────

-- Person – base entity for all human actors in the system
CREATE TABLE IF NOT EXISTS Person (
    person_id INT          AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(45)  NOT NULL,
    email     VARCHAR(45)  NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL
);

-- ─────────────────────────────────────────────
-- ISA SUBTYPE : Customer  (IS-A Person)
-- ─────────────────────────────────────────────

-- Customer – any person who buys from the roastery
CREATE TABLE IF NOT EXISTS Customer (
    person_id   INT         PRIMARY KEY,
    type        VARCHAR(45) NOT NULL,   -- discriminator: 'individual' | 'business'
    location_id INT,
    FOREIGN KEY (person_id)   REFERENCES Person(person_id)   ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES Location(location_id)
);

-- Individual – retail customer subtype
CREATE TABLE IF NOT EXISTS Individual (
    person_id INT PRIMARY KEY,
    FOREIGN KEY (person_id) REFERENCES Customer(person_id) ON DELETE CASCADE
);

-- Business – wholesale / B2B customer subtype
CREATE TABLE IF NOT EXISTS Business (
    person_id           INT         PRIMARY KEY,
    registration_number VARCHAR(45) NOT NULL,
    TAX_id              VARCHAR(45) NOT NULL,
    business_type       VARCHAR(45) NOT NULL,
    FOREIGN KEY (person_id) REFERENCES Customer(person_id) ON DELETE CASCADE
);

-- ─────────────────────────────────────────────
-- ISA SUBTYPE : Employee  (IS-A Person)
-- ─────────────────────────────────────────────

-- Employee – staff member assigned to a branch
CREATE TABLE IF NOT EXISTS Employee (
    person_id  INT            PRIMARY KEY,
    role       VARCHAR(45)    NOT NULL,
    salary     DECIMAL(10, 2) NOT NULL CHECK (salary >= 0),
    hire_date  DATE           NOT NULL,
    branch_id  INT            NOT NULL,
    FOREIGN KEY (person_id) REFERENCES Person(person_id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES Branch(branch_id)
);

-- ─────────────────────────────────────────────
-- ISA SUBTYPE : Admin  (IS-A Person)
-- ─────────────────────────────────────────────

-- Admin – manages a specific branch
CREATE TABLE IF NOT EXISTS Admin (
    person_id INT PRIMARY KEY,
    branch_id INT NOT NULL,
    FOREIGN KEY (person_id) REFERENCES Person(person_id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES Branch(branch_id)
);

-- ─────────────────────────────────────────────
-- Item  (connected to Supplier, Employee, Customer)
-- ─────────────────────────────────────────────

-- Item – product sold or managed by the roastery
CREATE TABLE IF NOT EXISTS Item (
    item_id         INT            AUTO_INCREMENT PRIMARY KEY,
    price           DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    wholesale_price DECIMAL(10, 2) NOT NULL CHECK (wholesale_price > 0),
    item_image      VARCHAR(100),
    supplier_id     INT            NOT NULL,
    FOREIGN KEY (supplier_id) REFERENCES Supplier(supplier_id)
);

-- ─────────────────────────────────────────────
-- RELATIONSHIP : Employee ↔ Item  (manages, M:N)
-- ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS Employee_Item (
    person_id INT NOT NULL,
    item_id   INT NOT NULL,
    PRIMARY KEY (person_id, item_id),
    FOREIGN KEY (person_id) REFERENCES Employee(person_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id)   REFERENCES Item(item_id)       ON DELETE CASCADE
);
