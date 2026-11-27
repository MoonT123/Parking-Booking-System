-- One-time upgrade after 05_indexes.sql. Inspect preflight output before upgrading.
USE ParkingBookingSystem;

SELECT b.BookingID, b.UserID AS booking_user, v.UserID AS vehicle_owner
FROM BOOKING b JOIN VEHICLE v ON v.VehicleID = b.VehicleID
WHERE b.UserID <> v.UserID;

DELIMITER //
DROP PROCEDURE IF EXISTS check_dsa_upgrade //
CREATE PROCEDURE check_dsa_upgrade()
BEGIN
    IF EXISTS (SELECT 1 FROM BOOKING b JOIN VEHICLE v ON v.VehicleID = b.VehicleID
               WHERE b.UserID <> v.UserID) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Resolve booking/vehicle owner mismatches before upgrading';
    END IF;
END //
DELIMITER ;
CALL check_dsa_upgrade();
DROP PROCEDURE check_dsa_upgrade;

ALTER TABLE USER
    ADD COLUMN account_role ENUM('CUSTOMER', 'LOT_MANAGER', 'STAFF', 'ADMIN')
        NOT NULL DEFAULT 'CUSTOMER';

UPDATE USER u
JOIN (SELECT DISTINCT UserID FROM PARKINGLOT) owners ON owners.UserID = u.UserID
SET u.account_role = 'LOT_MANAGER';

ALTER TABLE VEHICLE ADD UNIQUE KEY uq_vehicle_owner (VehicleID, UserID);
ALTER TABLE BOOKING
    DROP FOREIGN KEY fk_booking_vehicle,
    ADD CONSTRAINT fk_booking_vehicle_owner FOREIGN KEY (VehicleID, UserID)
        REFERENCES VEHICLE (VehicleID, UserID) ON DELETE RESTRICT,
    ADD UNIQUE KEY uq_booking_visit (BookingID, lot_id, VehicleID);

CREATE TABLE PARKING_MAP_NODE (
    lot_id INT NOT NULL,
    node_id VARCHAR(32) NOT NULL,
    level_no SMALLINT UNSIGNED NOT NULL,
    x_m DECIMAL(8,2) NOT NULL,
    y_m DECIMAL(8,2) NOT NULL,
    node_type ENUM('JUNCTION', 'BAY', 'ENTRANCE', 'EXIT', 'ELEVATOR') NOT NULL,
    PRIMARY KEY (lot_id, node_id),
    CONSTRAINT chk_map_level CHECK (level_no >= 1),
    CONSTRAINT fk_map_node_lot FOREIGN KEY (lot_id) REFERENCES PARKINGLOT(lot_id)
        ON DELETE CASCADE
);

CREATE TABLE PARKING_MAP_EDGE (
    lot_id INT NOT NULL,
    from_node VARCHAR(32) NOT NULL,
    to_node VARCHAR(32) NOT NULL,
    distance_m DECIMAL(8,2) NOT NULL,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (lot_id, from_node, to_node),
    CONSTRAINT chk_map_edge CHECK (from_node <> to_node AND distance_m > 0 AND is_blocked IN (0,1)),
    CONSTRAINT fk_map_edge_from FOREIGN KEY (lot_id, from_node)
        REFERENCES PARKING_MAP_NODE(lot_id, node_id) ON DELETE CASCADE,
    CONSTRAINT fk_map_edge_to FOREIGN KEY (lot_id, to_node)
        REFERENCES PARKING_MAP_NODE(lot_id, node_id) ON DELETE CASCADE
);

ALTER TABLE PARKINGBAY
    ADD COLUMN level_no SMALLINT UNSIGNED NULL,
    ADD COLUMN zone_code VARCHAR(20) NULL,
    ADD COLUMN distance_to_exit_m DECIMAL(8,2) NULL,
    ADD COLUMN map_node_id VARCHAR(32) NULL,
    ADD CONSTRAINT chk_bay_level CHECK (level_no >= 1),
    ADD CONSTRAINT chk_bay_exit_distance CHECK (distance_to_exit_m >= 0),
    ADD UNIQUE KEY uq_bay_map_node (lot_id, map_node_id),
    ADD CONSTRAINT fk_bay_map_node FOREIGN KEY (lot_id, map_node_id)
        REFERENCES PARKING_MAP_NODE(lot_id, node_id);

CREATE TABLE VEHICLE_BAY_SIZE (
    vehicle_size ENUM('MOTORBIKE', 'CAR', 'SUV', 'VAN') NOT NULL,
    bay_size ENUM('SMALL', 'MEDIUM', 'LARGE') NOT NULL,
    fit_rank TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (vehicle_size, bay_size),
    UNIQUE KEY uq_vehicle_fit_rank (vehicle_size, fit_rank),
    CONSTRAINT chk_fit_rank CHECK (fit_rank >= 1)
);

INSERT INTO VEHICLE_BAY_SIZE VALUES
    ('MOTORBIKE', 'SMALL', 1), ('MOTORBIKE', 'MEDIUM', 2), ('MOTORBIKE', 'LARGE', 3),
    ('CAR', 'MEDIUM', 1), ('CAR', 'LARGE', 2),
    ('SUV', 'LARGE', 1), ('VAN', 'LARGE', 1);

ALTER TABLE PARKINGLOGS
    ADD COLUMN BookingID INT NULL,
    ADD CONSTRAINT fk_log_booking_visit FOREIGN KEY (BookingID, lot_id, VehicleID)
        REFERENCES BOOKING (BookingID, lot_id, VehicleID);

-- Link an existing visit only when one booking covers its check-in time.
UPDATE PARKINGLOGS l
JOIN (
    SELECT old_log.log_id, MIN(b.BookingID) AS BookingID
    FROM PARKINGLOGS old_log
    JOIN BOOKING b ON b.lot_id = old_log.lot_id AND b.VehicleID = old_log.VehicleID
        AND b.StartTime <= old_log.check_in_time AND b.EndTime > old_log.check_in_time
        AND b.BookingStatus IN ('CONFIRMED', 'COMPLETED')
    WHERE old_log.BookingID IS NULL
    GROUP BY old_log.log_id
    HAVING COUNT(*) = 1
) matches ON matches.log_id = l.log_id
SET l.BookingID = matches.BookingID;

CREATE OR REPLACE VIEW v_parking_overstay AS
SELECT l.log_id, v.LicensePlate, b.BookingID, b.EndTime, l.check_out_time,
       TIMESTAMPDIFF(MINUTE, b.EndTime, l.check_out_time) AS minutes_late,
       COALESCE(SUM(p.penalty_amount), 0) AS recorded_penalty_amount
FROM PARKINGLOGS l
JOIN BOOKING b ON b.BookingID = l.BookingID
JOIN VEHICLE v ON v.VehicleID = l.VehicleID
LEFT JOIN PENALTY p ON p.log_id = l.log_id
WHERE l.check_out_time > b.EndTime
GROUP BY l.log_id, v.LicensePlate, b.BookingID, b.EndTime, l.check_out_time;
