-- ============================================================================
-- PROJECT: PARKING BOOKING SYSTEM (FINAL DATABASE SCHEMA)
-- FILE: 04_transactions.sql
-- DESCRIPTION: Demonstrates core DML operations (INSERT, UPDATE, DELETE) and
--              multi-table ACID transactions (COMMIT & ROLLBACK).
--              Isolated with 'tx_' test keys so it can be re-run safely.
-- ============================================================================

USE ParkingBookingSystem;

-- ============================================================================
-- 0. CLEANUP PREVIOUS TEST DATA (Ensures script can run 2+ times cleanly)
-- ============================================================================
DELETE FROM PAYMENT
WHERE BookingID IN (
    SELECT BookingID FROM BOOKING
    WHERE UserID IN (SELECT UserID FROM USER WHERE Email IN ('tx_owner@parking.local', 'tx_driver@parking.local'))
);

DELETE FROM BOOKING
WHERE UserID IN (SELECT UserID FROM USER WHERE Email IN ('tx_owner@parking.local', 'tx_driver@parking.local'));

DELETE FROM PARKINGLOGS
WHERE VehicleID IN (SELECT VehicleID FROM VEHICLE WHERE LicensePlate IN ('51G-999.99', '51G-888.88'))
   OR lot_id IN (SELECT lot_id FROM PARKINGLOT WHERE LotName = 'TX Demo Lot');

DELETE FROM PARKINGLOT WHERE LotName = 'TX Demo Lot';
DELETE FROM VEHICLE WHERE LicensePlate IN ('51G-999.99', '51G-888.88');
DELETE FROM USER WHERE Email IN ('tx_owner@parking.local', 'tx_driver@parking.local');

-- ============================================================================
-- 1. PREREQUISITE SEED SETUP FOR TRANSACTION TESTING
-- ============================================================================
INSERT INTO USER (FullName, Email, PasswordHash, referrer_id)
VALUES ('TX Lot Owner', 'tx_owner@parking.local', 'hash_tx_owner_01', NULL);
SET @owner_id = LAST_INSERT_ID();

INSERT INTO USER (FullName, Email, PasswordHash, referrer_id)
VALUES ('TX Test Driver', 'tx_driver@parking.local', 'hash_tx_driver_02', @owner_id);
SET @driver_id = LAST_INSERT_ID();

INSERT INTO PARKINGLOT (LotName, Address, opening_hours, number_of_levels, UserID)
VALUES ('TX Demo Lot', '268 Ly Thuong Kiet, District 10, HCMC', '06:00-23:00', 3, @owner_id);
SET @lot_id = LAST_INSERT_ID();

INSERT INTO PARKINGBAY (lot_id, bay_number, bay_status, bay_size, bay_type) VALUES
(@lot_id, 'TX-01', 'AVAILABLE', 'MEDIUM', 'STANDARD'),
(@lot_id, 'TX-02', 'AVAILABLE', 'LARGE', 'VIP'),
(@lot_id, 'TX-03', 'AVAILABLE', 'SMALL', 'STANDARD');

-- ============================================================================
-- 2. CORE DML OPERATIONS (INSERT, UPDATE, DELETE)
-- ============================================================================

-- 2.1. INSERT: Add user phone numbers and register two vehicles
INSERT INTO USER_PHONENUMBER (UserID, PhoneNumber) VALUES
(@driver_id, '0909111222'),
(@driver_id, '0909333444');

INSERT INTO VEHICLE (LicensePlate, Make, Model, Color, VehicleSize, UserID)
VALUES ('51G-999.99', 'Mazda', 'CX-5', 'Black', 'SUV', @driver_id);
SET @vehicle_id = LAST_INSERT_ID();

INSERT INTO VEHICLE (LicensePlate, Make, Model, Color, VehicleSize, UserID)
VALUES ('51G-888.88', 'Honda', 'AirBlade', 'Red', 'MOTORBIKE', @driver_id);

-- 2.2. UPDATE: Update vehicle color and set bay TX-03 under maintenance
UPDATE VEHICLE
SET Color = 'Metallic Gray'
WHERE VehicleID = @vehicle_id;

UPDATE PARKINGBAY
SET bay_status = 'MAINTENANCE'
WHERE lot_id = @lot_id AND bay_number = 'TX-03';

-- 2.3. DELETE: Remove the secondary unused vehicle record
DELETE FROM VEHICLE
WHERE LicensePlate = '51G-888.88' AND UserID = @driver_id;

-- Verify DML results
SELECT VehicleID, LicensePlate, Make, Model, Color, VehicleSize, UserID
FROM VEHICLE
WHERE UserID = @driver_id;

-- ============================================================================
-- 3. TRANSACTION 1 (COMMIT): ATOMIC BOOKING + PAYMENT + BAY RESERVATION
-- Scenario: Customer reserves bay TX-01 and pays via E-Wallet.
--           All 3 tables (BOOKING, PAYMENT, PARKINGBAY) update atomically.
-- ============================================================================
START TRANSACTION;

-- Step 3.1: Create confirmed booking record
INSERT INTO BOOKING (StartTime, EndTime, TotalAmount, BookingStatus, UserID, VehicleID, lot_id, bay_number)
VALUES ('2026-11-20 08:00:00', '2026-11-20 10:30:00', 50000.00, 'CONFIRMED', @driver_id, @vehicle_id, @lot_id, 'TX-01');
SET @booking_id = LAST_INSERT_ID();

-- Step 3.2: Record successful payment transaction
INSERT INTO PAYMENT (AmountPaid, PaymentMethod, TransactionID, PaymentStatus, BookingID)
VALUES (50000.00, 'E_WALLET', 'TXN_DEMO_20261120_01', 'SUCCESS', @booking_id);

-- Step 3.3: Synchronize bay status from AVAILABLE to RESERVED
UPDATE PARKINGBAY
SET bay_status = 'RESERVED'
WHERE lot_id = @lot_id AND bay_number = 'TX-01';

COMMIT;

-- Verify Transaction 1 (Committed state across 3 tables)
SELECT
    b.BookingID,
    b.BookingStatus,
    p.PaymentID,
    p.AmountPaid,
    p.PaymentStatus,
    pb.bay_number,
    pb.bay_status AS CurrentBayStatus
FROM BOOKING b
JOIN PAYMENT p ON b.BookingID = p.BookingID
JOIN PARKINGBAY pb ON b.lot_id = pb.lot_id AND b.bay_number = pb.bay_number
WHERE b.BookingID = @booking_id;

-- ============================================================================
-- 4. TRANSACTION 2 (ROLLBACK): ABORTED CHECK-IN / RESERVATION FAILURE
-- Scenario: Staff starts check-in for bay TX-02 (updates PARKINGBAY to OCCUPIED
--           and inserts a PARKINGLOGS row), then an error/cancellation occurs.
--           ROLLBACK reverts both tables back to their original consistent state.
-- ============================================================================
START TRANSACTION;

-- Step 4.1: Temporarily mark bay TX-02 as OCCUPIED
UPDATE PARKINGBAY
SET bay_status = 'OCCUPIED'
WHERE lot_id = @lot_id AND bay_number = 'TX-02';

-- Step 4.2: Temporarily insert barrier entry log
INSERT INTO PARKINGLOGS (check_in_time, lot_id, UserID, VehicleID)
VALUES ('2026-11-20 11:00:00', @lot_id, @owner_id, @vehicle_id);

-- Step 4.3: Abort transaction due to validation/payment failure
ROLLBACK;

-- Verify Transaction 2 (TX-02 remains AVAILABLE and 0 logs exist for TX Demo Lot)
SELECT
    pb.lot_id,
    pb.bay_number,
    pb.bay_status AS BayStatusAfterRollback,
    (SELECT COUNT(*) FROM PARKINGLOGS pl WHERE pl.lot_id = pb.lot_id) AS LogsCountAfterRollback
FROM PARKINGBAY pb
WHERE pb.lot_id = @lot_id AND pb.bay_number = 'TX-02';