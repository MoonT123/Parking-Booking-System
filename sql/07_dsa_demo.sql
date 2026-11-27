-- Optional fixtures for an upgraded development database. Run once on the sample DB.
USE ParkingBookingSystem;
START TRANSACTION;

INSERT INTO USER (FullName, Email, PasswordHash, account_role)
VALUES ('DSA Demo Manager', 'dsa-manager@example.invalid', 'demo-disabled', 'LOT_MANAGER');
SET @manager = LAST_INSERT_ID();

INSERT INTO USER (FullName, Email, PasswordHash)
SELECT CONCAT('DSA Driver ', n), CONCAT('dsa-driver-', n, '@example.invalid'), 'demo-disabled'
FROM (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
    SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) drivers;

INSERT INTO VEHICLE (LicensePlate, Make, Model, VehicleSize, UserID)
SELECT CONCAT('DSA-', RIGHT(SUBSTRING_INDEX(Email, '@', 1), 1)), 'Demo', 'City', 'CAR', UserID
FROM USER WHERE Email LIKE 'dsa-driver-_%@example.invalid';

INSERT INTO PARKINGLOT (LotName, Address, number_of_levels, UserID)
VALUES ('DSA Riverside', 'Demo East', 2, @manager),
       ('DSA Central', 'Demo Center', 2, @manager),
       ('DSA West', 'Demo West', 2, @manager);

CREATE TEMPORARY TABLE dsa_numbers (n INT PRIMARY KEY);
INSERT INTO dsa_numbers VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10);

INSERT INTO PARKING_MAP_NODE (lot_id, node_id, level_no, x_m, y_m, node_type)
SELECT lot_id, 'EXIT', 1, 0, 0, 'EXIT' FROM PARKINGLOT WHERE UserID = @manager
UNION ALL
SELECT lot_id, 'LIFT', 2, 0, 0, 'ELEVATOR' FROM PARKINGLOT WHERE UserID = @manager;

INSERT INTO PARKING_MAP_NODE (lot_id, node_id, level_no, x_m, y_m, node_type)
SELECT p.lot_id, CONCAT(f.n, '-', LPAD(i.n, 2, '0')), f.n, i.n * 4, 0, 'BAY'
FROM PARKINGLOT p CROSS JOIN (SELECT 1 AS n UNION ALL SELECT 2) f CROSS JOIN dsa_numbers i
WHERE p.UserID = @manager;

INSERT INTO PARKING_MAP_EDGE (lot_id, from_node, to_node, distance_m)
SELECT lot_id, IF(level_no = 1, 'EXIT', 'LIFT'), node_id, x_m
FROM PARKING_MAP_NODE WHERE node_type = 'BAY'
    AND lot_id IN (SELECT lot_id FROM PARKINGLOT WHERE UserID = @manager);

INSERT INTO PARKING_MAP_EDGE (lot_id, from_node, to_node, distance_m)
SELECT lot_id, node_id, IF(level_no = 1, 'EXIT', 'LIFT'), x_m
FROM PARKING_MAP_NODE WHERE node_type = 'BAY' AND node_id <> '1-10'
    AND lot_id IN (SELECT lot_id FROM PARKINGLOT WHERE UserID = @manager);

INSERT INTO PARKING_MAP_EDGE (lot_id, from_node, to_node, distance_m, is_blocked)
SELECT lot_id, '1-10', 'EXIT', 40, TRUE FROM PARKINGLOT WHERE UserID = @manager;
INSERT INTO PARKING_MAP_EDGE (lot_id, from_node, to_node, distance_m)
SELECT lot_id, '1-10', '1-09', 4 FROM PARKINGLOT WHERE UserID = @manager;
INSERT INTO PARKING_MAP_EDGE (lot_id, from_node, to_node, distance_m)
SELECT lot_id, 'LIFT', 'EXIT', 12 FROM PARKINGLOT WHERE UserID = @manager
UNION ALL
SELECT lot_id, 'EXIT', 'LIFT', 12 FROM PARKINGLOT WHERE UserID = @manager;

INSERT INTO PARKINGBAY (lot_id, bay_number, bay_size, level_no, zone_code, distance_to_exit_m, map_node_id)
SELECT n.lot_id, n.node_id,
       CASE MOD(CAST(RIGHT(n.node_id, 2) AS UNSIGNED), 3)
           WHEN 0 THEN 'SMALL' WHEN 1 THEN 'MEDIUM' ELSE 'LARGE' END,
       n.level_no, IF(n.level_no = 1, 'GROUND', 'UPPER'),
       n.x_m + IF(n.level_no = 1, 0, 12), n.node_id
FROM PARKING_MAP_NODE n JOIN PARKINGLOT p ON p.lot_id = n.lot_id
WHERE p.UserID = @manager AND n.node_type = 'BAY';

INSERT INTO TARIFF (day_of_week, start_hour, end_hour, hourly_rate)
VALUES ('ALL', '00:00:00', '24:00:00', 16000);
SET @fallback = LAST_INSERT_ID();

-- Give legacy sample lots and demo lots an all-day fallback.
INSERT INTO APPLIESTARIF (lot_id, Tariff_ID, effective_date)
SELECT lot_id, @fallback, '2026-01-01' FROM PARKINGLOT
WHERE UserID = @manager OR UserID IN (
    SELECT UserID FROM USER WHERE Email IN (
        'an.nguyen@email.com', 'binh.tran@email.com', 'cuong.le@email.com',
        'duc.pham@email.com', 'em.vo@email.com'
    )
);

-- Reserve every bay in the first demo lot for the same one-hour window,
-- using a vehicle that fits each bay and belongs to its booking user.
SET @demo_lot = (SELECT MIN(lot_id) FROM PARKINGLOT WHERE UserID = @manager);
SET @demo_day = CURRENT_DATE + INTERVAL 1 DAY;

INSERT INTO VEHICLE (LicensePlate, Make, Model, VehicleSize, UserID)
SELECT CONCAT('DSA-FULL-', bay_number), 'Demo', 'Fleet',
       CASE bay_size WHEN 'SMALL' THEN 'MOTORBIKE' WHEN 'MEDIUM' THEN 'CAR' ELSE 'SUV' END,
       @manager
FROM PARKINGBAY WHERE lot_id = @demo_lot;

INSERT INTO BOOKING (StartTime, EndTime, TotalAmount, BookingStatus, UserID, VehicleID, lot_id, bay_number)
SELECT TIMESTAMP(@demo_day, '09:00:00'), TIMESTAMP(@demo_day, '10:00:00'),
       16000, 'CONFIRMED', v.UserID, v.VehicleID, b.lot_id, b.bay_number
FROM PARKINGBAY b
JOIN VEHICLE v ON v.LicensePlate = CONCAT('DSA-FULL-', b.bay_number)
WHERE b.lot_id = @demo_lot;

INSERT INTO BOOKING (StartTime, EndTime, TotalAmount, BookingStatus, UserID, VehicleID, lot_id, bay_number)
SELECT TIMESTAMP(@demo_day, '10:00:00'), TIMESTAMP(@demo_day, '11:00:00'),
       16000, 'CONFIRMED', v.UserID, v.VehicleID, @demo_lot, '1-01'
FROM VEHICLE v WHERE LicensePlate = 'DSA-1';
INSERT INTO BOOKING (StartTime, EndTime, TotalAmount, BookingStatus, UserID, VehicleID, lot_id, bay_number)
SELECT TIMESTAMP(@demo_day, '09:30:00'), TIMESTAMP(@demo_day, '10:30:00'),
       16000, 'CANCELLED', v.UserID, v.VehicleID, @demo_lot, '1-01'
FROM VEHICLE v WHERE LicensePlate = 'DSA-2';
INSERT INTO CANCELLATION (BookingID, reason) VALUES (LAST_INSERT_ID(), 'DSA demo fixture');

DROP TEMPORARY TABLE dsa_numbers;
COMMIT;

SELECT p.LotName, COUNT(*) AS bay_count
FROM PARKINGLOT p JOIN PARKINGBAY pb ON pb.lot_id = p.lot_id
WHERE p.UserID = @manager GROUP BY p.lot_id, p.LotName;
