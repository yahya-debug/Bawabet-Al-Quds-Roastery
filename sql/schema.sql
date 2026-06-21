-- ============================================================
--  Bawabet Al-Quds — full schema
--  Drop and recreate all tables in dependency order.
-- ============================================================

CREATE DATABASE IF NOT EXISTS bawabet_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE bawabet_db;

SET FOREIGN_KEY_CHECKS = 0;

-- ── Core identity ────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS Location (
  location_id INT          NOT NULL AUTO_INCREMENT,
  street      VARCHAR(100) NOT NULL,
  city        VARCHAR(45)  NOT NULL,
  zip         VARCHAR(45)  NOT NULL,
  PRIMARY KEY (location_id)
);

CREATE TABLE IF NOT EXISTS Person (
  person_id INT          NOT NULL AUTO_INCREMENT,
  name      VARCHAR(45)  NOT NULL,
  email     VARCHAR(45)  NOT NULL,
  password  VARCHAR(100) NOT NULL,
  phone     VARCHAR(45)  DEFAULT NULL,
  PRIMARY KEY (person_id),
  UNIQUE KEY uq_person_email          (email),
  UNIQUE KEY uq_person_name_email     (name, email),
  CONSTRAINT chk_person_email CHECK (email LIKE '%@%')
);

-- ── Branch ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS Branch (
  branch_id   INT         NOT NULL AUTO_INCREMENT,
  location_id INT         NOT NULL,
  branch_name VARCHAR(45) DEFAULT NULL,
  PRIMARY KEY (branch_id),
  CONSTRAINT fk_branch_location FOREIGN KEY (location_id)
    REFERENCES Location (location_id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- ── Person sub-types ─────────────────────────────────────────

CREATE TABLE IF NOT EXISTS Customer (
  person_id   INT         NOT NULL,
  type        VARCHAR(45) NOT NULL,
  location_id INT         DEFAULT NULL,
  PRIMARY KEY (person_id),
  CONSTRAINT fk_customer_person   FOREIGN KEY (person_id)
    REFERENCES Person   (person_id)   ON DELETE CASCADE,
  CONSTRAINT fk_customer_location FOREIGN KEY (location_id)
    REFERENCES Location (location_id) ON DELETE SET NULL,
  CONSTRAINT chk_customer_type CHECK (type IN ('individual', 'business'))
);

CREATE TABLE IF NOT EXISTS Individual (
  person_id INT NOT NULL,
  PRIMARY KEY (person_id),
  CONSTRAINT fk_individual_customer FOREIGN KEY (person_id)
    REFERENCES Customer (person_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Business (
  person_id           INT         NOT NULL,
  registration_number VARCHAR(45) NOT NULL,
  TAX_id              VARCHAR(45) NOT NULL,
  business_type       VARCHAR(45) NOT NULL,
  PRIMARY KEY (person_id),
  CONSTRAINT fk_business_customer FOREIGN KEY (person_id)
    REFERENCES Customer (person_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Admin (
  person_id INT NOT NULL AUTO_INCREMENT,
  branch_id INT NOT NULL,
  PRIMARY KEY (person_id),
  CONSTRAINT fk_admin_person FOREIGN KEY (person_id)
    REFERENCES Person  (person_id) ON DELETE CASCADE,
  CONSTRAINT fk_admin_branch FOREIGN KEY (branch_id)
    REFERENCES Branch  (branch_id)
);

CREATE TABLE IF NOT EXISTS Employee (
  person_id    INT            NOT NULL,
  role         VARCHAR(45)    NOT NULL,
  salary       DECIMAL(10, 0) NOT NULL,
  hire_date    DATE           NOT NULL,
  branch_id    INT            DEFAULT NULL,
  warehouse_id INT            DEFAULT NULL,
  PRIMARY KEY (person_id),
  CONSTRAINT fk_employee_person    FOREIGN KEY (person_id)
    REFERENCES Person    (person_id) ON DELETE CASCADE,
  CONSTRAINT fk_employee_branch    FOREIGN KEY (branch_id)
    REFERENCES Branch    (branch_id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT fk_employee_warehouse FOREIGN KEY (warehouse_id)
    REFERENCES Warehouse (warehouse_id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT chk_employee_salary   CHECK (salary >= 0),
  CONSTRAINT chk_employee_assign   CHECK (
    (branch_id IS NOT NULL AND warehouse_id IS NULL) OR
    (branch_id IS NULL    AND warehouse_id IS NOT NULL)
  )
);

-- ── Supplier & Items ─────────────────────────────────────────

CREATE TABLE IF NOT EXISTS Supplier (
  supplier_id INT          NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL,
  email       VARCHAR(100) DEFAULT NULL,
  phone       VARCHAR(45)  DEFAULT NULL,
  PRIMARY KEY (supplier_id)
);

CREATE TABLE IF NOT EXISTS Item (
  item_id         INT          NOT NULL AUTO_INCREMENT,
  name            VARCHAR(100) NOT NULL DEFAULT '',
  item_type       VARCHAR(45)  DEFAULT NULL,
  price           DECIMAL(10, 0) DEFAULT NULL,
  wholesale_price DECIMAL(10, 0) DEFAULT NULL,
  item_image      VARCHAR(100)   DEFAULT NULL,
  image_path      VARCHAR(500)   DEFAULT NULL,
  supplier_id     INT            NOT NULL,
  PRIMARY KEY (item_id),
  CONSTRAINT fk_item_supplier FOREIGN KEY (supplier_id)
    REFERENCES Supplier (supplier_id) ON DELETE RESTRICT ON UPDATE NO ACTION,
  CONSTRAINT chk_item_price     CHECK (price IS NULL OR price >= 0),
  CONSTRAINT chk_item_wholesale CHECK (wholesale_price IS NULL OR wholesale_price >= 0),
  CONSTRAINT chk_item_type      CHECK (item_type IN ('Coffee', 'Roasts', 'Spice', 'Package'))
);

CREATE TABLE IF NOT EXISTS SupplierItem (
  supplier_id  INT            NOT NULL,
  item_id      INT            NOT NULL,
  supply_price DECIMAL(10, 2) DEFAULT NULL,
  PRIMARY KEY (supplier_id, item_id),
  CONSTRAINT fk_si_supplier FOREIGN KEY (supplier_id)
    REFERENCES Supplier (supplier_id) ON DELETE CASCADE,
  CONSTRAINT fk_si_item    FOREIGN KEY (item_id)
    REFERENCES Item     (item_id)     ON DELETE CASCADE,
  CONSTRAINT chk_si_price  CHECK (supply_price IS NULL OR supply_price >= 0)
);

CREATE TABLE IF NOT EXISTS BranchInventory (
  branch_id INT NOT NULL,
  item_id   INT NOT NULL,
  quantity  INT NOT NULL DEFAULT 0,
  PRIMARY KEY (branch_id, item_id),
  CONSTRAINT fk_bi_branch FOREIGN KEY (branch_id)
    REFERENCES Branch (branch_id) ON DELETE CASCADE,
  CONSTRAINT fk_bi_item   FOREIGN KEY (item_id)
    REFERENCES Item   (item_id)   ON DELETE CASCADE,
  CONSTRAINT chk_bi_qty   CHECK (quantity >= 0)
);

-- ── Item sub-types ───────────────────────────────────────────

CREATE TABLE IF NOT EXISTS Package (
  package_id  INT          NOT NULL,
  name        VARCHAR(100) NOT NULL,
  description TEXT         DEFAULT NULL,
  PRIMARY KEY (package_id),
  CONSTRAINT fk_package_item FOREIGN KEY (package_id)
    REFERENCES Item (item_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS PackageItem (
  package_id INT NOT NULL,
  item_id    INT NOT NULL,
  quantity   INT NOT NULL DEFAULT 1,
  PRIMARY KEY (package_id, item_id),
  CONSTRAINT fk_pkgi_package FOREIGN KEY (package_id)
    REFERENCES Package (package_id) ON DELETE CASCADE,
  CONSTRAINT fk_pkgi_item    FOREIGN KEY (item_id)
    REFERENCES Item    (item_id),
  CONSTRAINT chk_pkgi_qty    CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS Coffee (
  item_id INT NOT NULL,
  PRIMARY KEY (item_id),
  CONSTRAINT fk_coffee_item FOREIGN KEY (item_id)
    REFERENCES Item (item_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Roasts (
  item_id INT NOT NULL,
  PRIMARY KEY (item_id),
  CONSTRAINT fk_roasts_item FOREIGN KEY (item_id)
    REFERENCES Item (item_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Spice (
  item_id INT NOT NULL,
  PRIMARY KEY (item_id),
  CONSTRAINT fk_spice_item FOREIGN KEY (item_id)
    REFERENCES Item (item_id) ON DELETE CASCADE
);

-- ── Orders ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS Cart (
  person_id INT NOT NULL,
  item_id   INT NOT NULL,
  quantity  INT NOT NULL DEFAULT 1,
  branch_id INT NOT NULL DEFAULT 0,
  PRIMARY KEY (person_id, item_id),
  CONSTRAINT fk_cart_customer FOREIGN KEY (person_id)
    REFERENCES Customer (person_id) ON DELETE CASCADE,
  CONSTRAINT fk_cart_item     FOREIGN KEY (item_id)
    REFERENCES Item     (item_id)   ON DELETE CASCADE,
  CONSTRAINT chk_cart_qty     CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS `Order` (
  order_id       INT            NOT NULL AUTO_INCREMENT,
  person_id      INT            NOT NULL,
  branch_id      INT            DEFAULT NULL,
  order_date     DATETIME       DEFAULT CURRENT_TIMESTAMP,
  status         VARCHAR(45)    DEFAULT 'pending',
  payment_method VARCHAR(45)    DEFAULT NULL,
  total          DECIMAL(10, 2) DEFAULT 0.00,
  PRIMARY KEY (order_id),
  CONSTRAINT fk_order_person  FOREIGN KEY (person_id)
    REFERENCES Person (person_id) ON DELETE CASCADE,
  CONSTRAINT fk_order_branch  FOREIGN KEY (branch_id)
    REFERENCES Branch (branch_id) ON DELETE SET NULL,
  CONSTRAINT chk_order_status CHECK (status IN ('pending', 'delivered')),
  CONSTRAINT chk_order_total  CHECK (total >= 0)
);

CREATE TABLE IF NOT EXISTS OrderItem (
  order_id   INT            NOT NULL,
  item_id    INT            NOT NULL,
  quantity   INT            NOT NULL DEFAULT 1,
  unit_price DECIMAL(10, 2) NOT NULL,
  PRIMARY KEY (order_id, item_id),
  CONSTRAINT fk_oi_order   FOREIGN KEY (order_id)
    REFERENCES `Order` (order_id) ON DELETE CASCADE,
  CONSTRAINT fk_oi_item    FOREIGN KEY (item_id)
    REFERENCES Item    (item_id),
  CONSTRAINT chk_oi_qty    CHECK (quantity > 0),
  CONSTRAINT chk_oi_price  CHECK (unit_price >= 0)
);

CREATE TABLE IF NOT EXISTS Delivery (
  delivery_id  INT          NOT NULL AUTO_INCREMENT,
  order_id     INT          NOT NULL,
  address      VARCHAR(200) NOT NULL,
  delivered_at DATETIME     DEFAULT NULL,
  status       VARCHAR(45)  DEFAULT 'pending',
  PRIMARY KEY (delivery_id),
  CONSTRAINT fk_delivery_order FOREIGN KEY (order_id)
    REFERENCES `Order` (order_id) ON DELETE CASCADE,
  CONSTRAINT chk_delivery_status CHECK (status IN ('pending', 'delivered'))
);

-- ── Extras ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS Discount (
  discount_id INT            NOT NULL AUTO_INCREMENT,
  code        VARCHAR(50)    NOT NULL,
  percent     DECIMAL(5, 2)  NOT NULL,
  valid_until DATE           DEFAULT NULL,
  max_uses    INT            DEFAULT NULL,
  PRIMARY KEY (discount_id),
  UNIQUE KEY uq_discount_code (code),
  CONSTRAINT chk_discount_pct  CHECK (percent BETWEEN 0 AND 100),
  CONSTRAINT chk_discount_uses CHECK (max_uses IS NULL OR max_uses > 0)
);

CREATE TABLE IF NOT EXISTS Review (
  review_id   INT      NOT NULL AUTO_INCREMENT,
  person_id   INT      NOT NULL,
  item_id     INT      NOT NULL,
  rating      TINYINT  NOT NULL,
  comment     TEXT     DEFAULT NULL,
  review_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (review_id),
  CONSTRAINT fk_review_person  FOREIGN KEY (person_id)
    REFERENCES Person (person_id) ON DELETE CASCADE,
  CONSTRAINT fk_review_item    FOREIGN KEY (item_id)
    REFERENCES Item   (item_id)   ON DELETE CASCADE,
  CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5)
);

-- ── Warehouse ────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS Warehouse (
  warehouse_id INT          NOT NULL AUTO_INCREMENT,
  location_id  INT          NOT NULL,
  name         VARCHAR(100) NOT NULL,
  PRIMARY KEY (warehouse_id),
  CONSTRAINT fk_warehouse_location FOREIGN KEY (location_id)
    REFERENCES Location (location_id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS WarehouseInventory (
  warehouse_id INT NOT NULL,
  item_id      INT NOT NULL,
  quantity     INT NOT NULL DEFAULT 0,
  PRIMARY KEY (warehouse_id, item_id),
  CONSTRAINT fk_wi_warehouse FOREIGN KEY (warehouse_id)
    REFERENCES Warehouse (warehouse_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_wi_item      FOREIGN KEY (item_id)
    REFERENCES Item (item_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT chk_wi_qty      CHECK (quantity >= 0)
);

SET FOREIGN_KEY_CHECKS = 1;
