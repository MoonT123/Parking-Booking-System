-- ============================================================================
-- PROJECT: PARKING BOOKING SYSTEM (FINAL DATABASE SCHEMA)
-- FILE: 01_create_database.sql
-- DESCRIPTION: DDL script creating 12 normalized (BCNF) tables, PKs, FKs,
--              and integrity constraints. Re-running this script deletes the database.
-- ============================================================================

DROP DATABASE IF EXISTS ParkingBookingSystem;
CREATE DATABASE ParkingBookingSystem
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE ParkingBookingSystem;

-- 1. USER (Stores user accounts and optional self-referencing referral)
CREATE TABLE USER (
    UserID INT AUTO_INCREMENT PRIMARY KEY,
    FullName VARCHAR(100) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
    CreatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    referrer_id INT NULL,
    CONSTRAINT fk_user_referrer
        FOREIGN KEY (referrer_id) REFERENCES USER(UserID)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- 2. USER_PHONENUMBER (Stores multivalued phone numbers of users)
CREATE TABLE USER_PHONENUMBER (
    UserID INT NOT NULL,
    PhoneNumber VARCHAR(20) NOT NULL,
    PRIMARY KEY (UserID, PhoneNumber),
    CONSTRAINT fk_userphone_user
        FOREIGN KEY (UserID) REFERENCES USER(UserID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- 3. VEHICLE (Stores registered vehicles owned by users)
CREATE TABLE VEHICLE (
    VehicleID INT AUTO_INCREMENT PRIMARY KEY,
    LicensePlate VARCHAR(20) NOT NULL UNIQUE,
    Make VARCHAR(50) NOT NULL,
    Model VARCHAR(50) NOT NULL,
    Color VARCHAR(30) NULL,
    VehicleSize ENUM('MOTORBIKE', 'CAR', 'SUV', 'VAN') NOT NULL,
    UserID INT NOT NULL,
    CONSTRAINT fk_vehicle_user
        FOREIGN KEY (UserID) REFERENCES USER(UserID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- 4. PARKINGLOT (Stores parking facility details owned by users)
CREATE TABLE PARKINGLOT (
    lot_id INT AUTO_INCREMENT PRIMARY KEY,
    LotName VARCHAR(100) NOT NULL,
    Address VARCHAR(255) NOT NULL,
    opening_hours VARCHAR(50) NOT NULL DEFAULT '00:00-23:59',
    number_of_levels INT NOT NULL DEFAULT 1,
    UserID INT NOT NULL,
    CONSTRAINT chk_parkinglot_levels CHECK (number_of_levels >= 1),
    CONSTRAINT fk_parkinglot_owner
        FOREIGN KEY (UserID) REFERENCES USER(UserID)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- 5. PARKINGBAY (Weak entity identified by lot_id + bay_number)
CREATE TABLE PARKINGBAY (
    lot_id INT NOT NULL,
    bay_number VARCHAR(20) NOT NULL,
    bay_status ENUM('AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    bay_size ENUM('SMALL', 'MEDIUM', 'LARGE') NOT NULL DEFAULT 'MEDIUM',
    bay_type ENUM('STANDARD', 'VIP', 'EV_CHARGING', 'DISABLED') NOT NULL DEFAULT 'STANDARD',
    PRIMARY KEY (lot_id, bay_number),
    CONSTRAINT fk_parkingbay_lot
        FOREIGN KEY (lot_id) REFERENCES PARKINGLOT(lot_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- 6. BOOKING (Stores advance reservations for specific parking bays)
CREATE TABLE BOOKING (
    BookingID INT AUTO_INCREMENT PRIMARY KEY,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME NOT NULL,
    TotalAmount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    BookingStatus ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UserID INT NOT NULL,
    VehicleID INT NOT NULL,
    lot_id INT NOT NULL,
    bay_number VARCHAR(20) NOT NULL,
    CONSTRAINT chk_booking_time CHECK (EndTime > StartTime),
    CONSTRAINT chk_booking_amount CHECK (TotalAmount >= 0),
    CONSTRAINT fk_booking_user
        FOREIGN KEY (UserID) REFERENCES USER(UserID)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_booking_vehicle
        FOREIGN KEY (VehicleID) REFERENCES VEHICLE(VehicleID)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_booking_bay
        FOREIGN KEY (lot_id, bay_number) REFERENCES PARKINGBAY(lot_id, bay_number)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- 7. PARKINGLOGS (Tracks actual barrier entry and exit events)
CREATE TABLE PARKINGLOGS (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    check_in_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    check_out_time DATETIME NULL,
    lot_id INT NOT NULL,
    UserID INT NULL,
    VehicleID INT NOT NULL,
    CONSTRAINT chk_parkinglogs_time CHECK (check_out_time IS NULL OR check_out_time >= check_in_time),
    CONSTRAINT fk_logs_lot
        FOREIGN KEY (lot_id) REFERENCES PARKINGLOT(lot_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_logs_staff
        FOREIGN KEY (UserID) REFERENCES USER(UserID)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_logs_vehicle
        FOREIGN KEY (VehicleID) REFERENCES VEHICLE(VehicleID)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- 8. PENALTY (Stores overstay penalties linked to parking logs)
CREATE TABLE PENALTY (
    PenaltyID INT AUTO_INCREMENT PRIMARY KEY,
    overstay_minutes INT NOT NULL,
    penalty_amount DECIMAL(10, 2) NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_status ENUM('UNPAID', 'PAID', 'WAIVED') NOT NULL DEFAULT 'UNPAID',
    log_id INT NOT NULL,
    CONSTRAINT chk_penalty_minutes CHECK (overstay_minutes > 0),
    CONSTRAINT chk_penalty_amount CHECK (penalty_amount >= 0),
    CONSTRAINT fk_penalty_log
        FOREIGN KEY (log_id) REFERENCES PARKINGLOGS(log_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- 9. PAYMENT (Stores payment transactions for bookings)
CREATE TABLE PAYMENT (
    PaymentID INT AUTO_INCREMENT PRIMARY KEY,
    AmountPaid DECIMAL(10, 2) NOT NULL,
    PaymentMethod ENUM('CREDIT_CARD', 'DEBIT_CARD', 'E_WALLET', 'BANK_TRANSFER', 'CASH') NOT NULL,
    TransactionID VARCHAR(100) NULL UNIQUE,
    PaymentStatus ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    BookingID INT NOT NULL,
    CONSTRAINT chk_payment_amount CHECK (AmountPaid > 0),
    CONSTRAINT fk_payment_booking
        FOREIGN KEY (BookingID) REFERENCES BOOKING(BookingID)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- 10. CANCELLATION (Stores cancellation records and refund amounts)
CREATE TABLE CANCELLATION (
    CancellationID INT AUTO_INCREMENT PRIMARY KEY,
    canceled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(255) NOT NULL,
    refund_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    BookingID INT NOT NULL,
    CONSTRAINT chk_cancellation_refund CHECK (refund_amount >= 0),
    CONSTRAINT fk_cancellation_booking
        FOREIGN KEY (BookingID) REFERENCES BOOKING(BookingID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- 11. TARIFF (Stores hourly pricing rules by day of week and time slot)
CREATE TABLE TARIFF (
    Tariff_ID INT AUTO_INCREMENT PRIMARY KEY,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY', 'ALL') NOT NULL,
    start_hour TIME NOT NULL,
    end_hour TIME NOT NULL,
    hourly_rate DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_tariff_hours CHECK (end_hour > start_hour),
    CONSTRAINT chk_tariff_rate CHECK (hourly_rate >= 0)
);

-- 12. APPLIESTARIF (Junction table mapping M:N between PARKINGLOT and TARIFF)
CREATE TABLE APPLIESTARIF (
    lot_id INT NOT NULL,
    Tariff_ID INT NOT NULL,
    effective_date DATE NOT NULL,
    PRIMARY KEY (lot_id, Tariff_ID),
    CONSTRAINT fk_appliestarif_lot
        FOREIGN KEY (lot_id) REFERENCES PARKINGLOT(lot_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_appliestarif_tariff
        FOREIGN KEY (Tariff_ID) REFERENCES TARIFF(Tariff_ID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
