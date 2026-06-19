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
  PRIMARY KEY (person_id),
  UNIQUE KEY uq_person_email          (email),
  UNIQUE KEY uq_person_name_email     (name, email)
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
    REFERENCES Location (location_id) ON DELETE SET NULL
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
  person_id INT            NOT NULL,
  role      VARCHAR(45)    NOT NULL,
  salary    DECIMAL(10, 0) NOT NULL,
  hire_date DATE           NOT NULL,
  branch_id INT            NOT NULL,
  PRIMARY KEY (person_id),
  CONSTRAINT fk_employee_person FOREIGN KEY (person_id)
    REFERENCES Person (person_id) ON DELETE CASCADE,
  CONSTRAINT fk_employee_branch FOREIGN KEY (branch_id)
    REFERENCES Branch (branch_id) ON DELETE NO ACTION ON UPDATE NO ACTION
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
  supplier_id     INT            DEFAULT NULL,
  PRIMARY KEY (item_id),
  CONSTRAINT fk_item_supplier FOREIGN KEY (supplier_id)
    REFERENCES Supplier (supplier_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS SupplierItem (
  supplier_id  INT            NOT NULL,
  item_id      INT            NOT NULL,
  supply_price DECIMAL(10, 2) DEFAULT NULL,
  PRIMARY KEY (supplier_id, item_id),
  CONSTRAINT fk_si_supplier FOREIGN KEY (supplier_id)
    REFERENCES Supplier (supplier_id) ON DELETE CASCADE,
  CONSTRAINT fk_si_item    FOREIGN KEY (item_id)
    REFERENCES Item     (item_id)     ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS BranchInventory (
  branch_id INT NOT NULL,
  item_id   INT NOT NULL,
  quantity  INT NOT NULL DEFAULT 0,
  PRIMARY KEY (branch_id, item_id),
  CONSTRAINT fk_bi_branch FOREIGN KEY (branch_id)
    REFERENCES Branch (branch_id) ON DELETE CASCADE,
  CONSTRAINT fk_bi_item   FOREIGN KEY (item_id)
    REFERENCES Item   (item_id)   ON DELETE CASCADE
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
    REFERENCES Item    (item_id)
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
    REFERENCES Item     (item_id)   ON DELETE CASCADE
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
  CONSTRAINT fk_order_person FOREIGN KEY (person_id)
    REFERENCES Person (person_id) ON DELETE CASCADE,
  CONSTRAINT fk_order_branch FOREIGN KEY (branch_id)
    REFERENCES Branch (branch_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS OrderItem (
  order_id   INT            NOT NULL,
  item_id    INT            NOT NULL,
  quantity   INT            NOT NULL DEFAULT 1,
  unit_price DECIMAL(10, 2) NOT NULL,
  PRIMARY KEY (order_id, item_id),
  CONSTRAINT fk_oi_order FOREIGN KEY (order_id)
    REFERENCES `Order` (order_id) ON DELETE CASCADE,
  CONSTRAINT fk_oi_item  FOREIGN KEY (item_id)
    REFERENCES Item    (item_id)
);

CREATE TABLE IF NOT EXISTS Delivery (
  delivery_id  INT          NOT NULL AUTO_INCREMENT,
  order_id     INT          NOT NULL,
  address      VARCHAR(200) NOT NULL,
  delivered_at DATETIME     DEFAULT NULL,
  status       VARCHAR(45)  DEFAULT 'pending',
  employee_id  INT          DEFAULT NULL,
  PRIMARY KEY (delivery_id),
  CONSTRAINT fk_delivery_order    FOREIGN KEY (order_id)
    REFERENCES `Order`  (order_id)   ON DELETE CASCADE,
  CONSTRAINT fk_delivery_employee FOREIGN KEY (employee_id)
    REFERENCES Employee (person_id)  ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS Payment (
  payment_id  INT            NOT NULL AUTO_INCREMENT,
  order_id    INT            NOT NULL,
  amount      DECIMAL(10, 2) NOT NULL,
  method      VARCHAR(45)    NOT NULL,
  employee_id INT            DEFAULT NULL,
  paid_at     DATETIME       DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (payment_id),
  CONSTRAINT fk_payment_order    FOREIGN KEY (order_id)
    REFERENCES `Order`  (order_id)  ON DELETE CASCADE,
  CONSTRAINT fk_payment_employee FOREIGN KEY (employee_id)
    REFERENCES Employee (person_id) ON DELETE SET NULL
);

-- ── Extras ───────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS Discount (
  discount_id INT            NOT NULL AUTO_INCREMENT,
  code        VARCHAR(50)    NOT NULL,
  percent     DECIMAL(5, 2)  NOT NULL,
  valid_until DATE           DEFAULT NULL,
  max_uses    INT            DEFAULT NULL,
  PRIMARY KEY (discount_id),
  UNIQUE KEY uq_discount_code (code)
);

CREATE TABLE IF NOT EXISTS Review (
  review_id   INT      NOT NULL AUTO_INCREMENT,
  person_id   INT      NOT NULL,
  item_id     INT      NOT NULL,
  rating      TINYINT  NOT NULL,
  comment     TEXT     DEFAULT NULL,
  review_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (review_id),
  CONSTRAINT fk_review_person FOREIGN KEY (person_id)
    REFERENCES Person (person_id) ON DELETE CASCADE,
  CONSTRAINT fk_review_item   FOREIGN KEY (item_id)
    REFERENCES Item   (item_id)   ON DELETE CASCADE
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
  CONSTRAINT fk_wi_item FOREIGN KEY (item_id)
    REFERENCES Item (item_id) ON DELETE CASCADE ON UPDATE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1;
