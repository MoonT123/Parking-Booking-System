-- ============================================================================
-- PROJECT: PARKING BOOKING SYSTEM (FINAL DATABASE SCHEMA)
-- FILE: 05_indexes.sql
-- DESCRIPTION: Safely creates composite and secondary indexes if not already
--              present, preserving foreign key backing indexes on re-runs.
-- ============================================================================

USE ParkingBookingSystem;

DROP PROCEDURE IF EXISTS DropIndexIfExists;

DELIMITER //
DROP PROCEDURE IF EXISTS CreateIndexIfNotExists //
CREATE PROCEDURE CreateIndexIfNotExists(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_columns VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @sql = CONCAT('CREATE INDEX ', p_index_name, ' ON ', p_table_name, ' (', p_index_columns, ')');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- ============================================================================
-- 1. CREATE CUSTOM INDEXES FOR PERFORMANCE OPTIMIZATION
-- ============================================================================

-- 1.1. Optimize double-booking overlap checks & bay availability search by time window
CALL CreateIndexIfNotExists('BOOKING', 'idx_booking_bay_time', 'lot_id, bay_number, StartTime, EndTime');

-- 1.2. Optimize retrieving a customer's booking history filtered/sorted by date
CALL CreateIndexIfNotExists('BOOKING', 'idx_booking_user_starttime', 'UserID, StartTime');

-- 1.3. Optimize filtering active/confirmed/expired reservations by schedule
CALL CreateIndexIfNotExists('BOOKING', 'idx_booking_status_time', 'BookingStatus, StartTime, EndTime');

-- 1.4. Optimize searching AVAILABLE bays by lot, status, and bay type
CALL CreateIndexIfNotExists('PARKINGBAY', 'idx_parkingbay_lot_status', 'lot_id, bay_status, bay_type');

-- 1.5. Optimize hourly parking utilization reports and active check-in lookups
CALL CreateIndexIfNotExists('PARKINGLOGS', 'idx_parkinglogs_lot_checkin', 'lot_id, check_in_time, check_out_time');

-- 1.6. Optimize financial revenue aggregation by payment status and payment timestamp
CALL CreateIndexIfNotExists('PAYMENT', 'idx_payment_status_paidat', 'PaymentStatus, paid_at');

-- 1.7. Optimize tracking UNPAID overstay penalties by issue date
CALL CreateIndexIfNotExists('PENALTY', 'idx_penalty_status_issued', 'payment_status, issued_at');

-- 1.8. Optimize looking up applicable hourly rates by day of week and time slot
CALL CreateIndexIfNotExists('TARIFF', 'idx_tariff_day_hours', 'day_of_week, start_hour, end_hour');

DROP PROCEDURE IF EXISTS CreateIndexIfNotExists;

-- ============================================================================
-- 2. VERIFY CREATED INDEXES & QUERY EXECUTION PLAN (EXPLAIN)
-- ============================================================================

-- 2.1. List all custom indexes created in ParkingBookingSystem
SELECT
    TABLE_NAME,
    INDEX_NAME,
    SEQ_IN_INDEX,
    COLUMN_NAME,
    INDEX_TYPE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'ParkingBookingSystem'
  AND INDEX_NAME LIKE 'idx_%'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- 2.2. Verify optimizer uses idx_booking_bay_time for availability/overlap queries
EXPLAIN FORMAT=TRADITIONAL
SELECT BookingID, UserID, StartTime, EndTime, BookingStatus
FROM BOOKING
WHERE lot_id = 1
  AND bay_number = 'TX-01'
  AND StartTime < '2026-11-20 12:00:00'
  AND EndTime > '2026-11-20 08:00:00';