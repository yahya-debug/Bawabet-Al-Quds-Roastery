-- Bawabet Al-Quds Roastery – Person & Connected Entities Schema
-- Student : Yahya Hasan  ID: 1242481
-- Course  : COMP333 – Database Systems  |  Instructor: Yousef Hassouneh

CREATE DATABASE bawabet_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bawabet_db;


-- Location – shared by Customer and Branch
CREATE TABLE Location (
    location_id INT          AUTO_INCREMENT PRIMARY KEY,
    street      VARCHAR(100) NOT NULL,
    city        VARCHAR(45)  NOT NULL,
    zip         VARCHAR(45)  NOT NULL
    );

-- Branch – physical store location
CREATE TABLE Branch (
    branch_id   INT         AUTO_INCREMENT PRIMARY KEY,
    branch_name VARCHAR(45) NOT NULL,
    location_id INT         NOT NULL,
    FOREIGN KEY (location_id) REFERENCES Location(location_id)
);

-- SUPERTYPE : Person

-- Person – base entity for all human actors in the system
CREATE TABLE Person (
    person_id INT          AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(45) NOT NULL,
    email     VARCHAR(45)  NOT NULL UNIQUE,
    password  VARCHAR(100) NOT NULL
);


-- ISA SUBTYPE : Customer  (IS-A Person)

-- Customer
CREATE TABLE Customer (
    person_id   INT         PRIMARY KEY,
    type        VARCHAR(45) NOT NULL,   -- discriminator: 'individual' | 'business'
    location_id INT,
    FOREIGN KEY (person_id)   REFERENCES Person(person_id)   ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES Location(location_id)
);

-- Individual – retail customer subtype
CREATE TABLE Individual (
    person_id INT PRIMARY KEY,
    FOREIGN KEY (person_id) REFERENCES Customer(person_id) ON DELETE CASCADE
);

-- Business – wholesale / B2B customer subtype
CREATE TABLE Business (
    person_id           INT         PRIMARY KEY,
    registration_number VARCHAR(45) NOT NULL,
    TAX_id              VARCHAR(45) NOT NULL,
    business_type       VARCHAR(45) NOT NULL,
    FOREIGN KEY (person_id) REFERENCES Customer(person_id) ON DELETE CASCADE
);


-- ISA SUBTYPE : Employee  (IS-A Person)

-- Employee
CREATE TABLE Employee (
    person_id  INT            PRIMARY KEY,
    role       VARCHAR(45)    NOT NULL,
    salary     DECIMAL(10, 2) NOT NULL CHECK (salary >= 0),
    hire_date  DATE           NOT NULL,
    branch_id  INT            NOT NULL,
    FOREIGN KEY (person_id) REFERENCES Person(person_id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES Branch(branch_id)
);

-- Admin – manages a specific branch
CREATE TABLE Admin (
    person_id INT PRIMARY KEY,
    branch_id INT NOT NULL,
    FOREIGN KEY (person_id) REFERENCES Person(person_id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES Branch(branch_id)
);


-- SEED DATA : one admin named yahya
-- Admin needs an existing Person row and an existing Branch row
-- so we seed a Location and a Branch first then the Person and finally the Admin

INSERT INTO Location (street, city, zip) VALUES ('Main Street 1', 'Ramallah', '00970');
INSERT INTO Branch (branch_name, location_id) VALUES ('Main Branch', LAST_INSERT_ID());

INSERT INTO Person (name, email, password) VALUES ('yahya', 'yahya@bawabet.com', 'admin123');
INSERT INTO Admin (person_id, branch_id)
VALUES (
    (SELECT person_id FROM Person WHERE name = 'yahya'),
    (SELECT branch_id FROM Branch WHERE branch_name = 'Main Branch')
);