-- ═══════════════════════════════════════════════════════════════════════════
-- Bawabet Al-Quds Roastery – Prototype SQL Queries
-- Student : Yahya Hasan  ID: 1242481
-- Course  : COMP333 – Database Systems  |  Instructor: Yousef Hassouneh
-- ═══════════════════════════════════════════════════════════════════════════

USE bawabet_db;

-- ───────────────────────────────────────────────────────────────────────────
-- FUNCTIONALITY 1  ─  INPUT / INSERT  (Sign-Up Screen)
--
-- English description:
--   Register a new individual customer.  The user fills in their name,
--   e-mail address, and password in the Sign-Up screen.  The system
--   executes three INSERT statements inside a single transaction:
--     1. Creates the base Customer row.
--     2. Creates the Individual sub-type row that marks this customer as
--        a retail (non-wholesale) buyer.
--     3. Opens an empty Cart so the customer can start shopping immediately.
--   If the e-mail already exists the transaction is rolled back and the
--   screen shows an error message.
-- ───────────────────────────────────────────────────────────────────────────

-- Step 1: insert base customer record (name, email, password from form)
INSERT INTO Customer (name, email, password)
VALUES (?, ?, ?);

-- Step 2: mark as Individual using the auto-generated customer_id
INSERT INTO Individual (customer_id)
VALUES (LAST_INSERT_ID());

-- Step 3: create an empty cart for the new customer
INSERT INTO Cart (customer_id)
VALUES (LAST_INSERT_ID());


-- ───────────────────────────────────────────────────────────────────────────
-- FUNCTIONALITY 2  ─  OUTPUT / RETRIEVE  (Home / Catalogue Screen)
--
-- English description:
--   Browse or search the product catalogue.  The user types an optional
--   keyword in the search bar and/or picks a product category
--   (All | Coffee | Roasts | Spice | Package).  The query retrieves
--   every matching Item row, sorted by category then name, and the result
--   is rendered as a grid of product cards on the Home screen.
--   Three variants are used depending on which inputs are provided.
-- ───────────────────────────────────────────────────────────────────────────

-- 2a: keyword AND category filter
SELECT item_id, name, price, wholesale_price, item_type
FROM   Item
WHERE  name      LIKE ?       -- e.g. '%arab%'
  AND  item_type  = ?         -- e.g. 'coffee'
ORDER  BY item_type, name;

-- 2b: keyword only (category selector = "All")
SELECT item_id, name, price, wholesale_price, item_type
FROM   Item
WHERE  name LIKE ?
ORDER  BY item_type, name;

-- 2c: no filter – load the full catalogue on first open
SELECT item_id, name, price, wholesale_price, item_type
FROM   Item
ORDER  BY item_type, name;


-- ───────────────────────────────────────────────────────────────────────────
-- ADDITIONAL QUERY  ─  Cart contents with grand total  (Cart Screen)
--
-- English description:
--   Given a logged-in customer's ID, retrieve every item currently in
--   their cart together with the quantity, unit price, and line subtotal.
--   A second aggregation query returns the cart grand total used to
--   populate the "Total" field on the Purchase panel.
-- ───────────────────────────────────────────────────────────────────────────

-- Cart line items
SELECT  i.name,
        ci.qty,
        i.price                    AS unit_price,
        (ci.qty * i.price)         AS line_total
FROM    CartItem ci
JOIN    Item  i  ON ci.item_id = i.item_id
JOIN    Cart  c  ON ci.cart_id = c.cart_id
WHERE   c.customer_id = ?
ORDER   BY i.name;

-- Cart grand total (aggregation: SUM)
SELECT  SUM(ci.qty * i.price)  AS cart_total
FROM    CartItem ci
JOIN    Item  i  ON ci.item_id = i.item_id
JOIN    Cart  c  ON ci.cart_id = c.cart_id
WHERE   c.customer_id = ?;
