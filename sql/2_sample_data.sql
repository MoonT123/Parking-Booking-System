-- ============================================================
-- 02_sample_data.sql
-- Parking Lot Booking System – Sample Data
-- >=15 rows/table | 12 tables | INSERT order follows FK dependency
-- ============================================================
-- NOTES:
--   * Matches 01_create_database.sql (ParkingBookingSystem)
--   * PARKINGBAY has composite PK (lot_id, bay_number)
--   * PARKINGLOGS has no BookingID column
--   * ENUM values are UPPERCASE as defined in DDL
--   * Table name is APPLIESTARIF (one F) per DDL
--   * Currency: VND
-- ============================================================

USE ParkingBookingSystem;

-- Disable FK checks to safely clear old data
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM CANCELLATION;
DELETE FROM PAYMENT;
DELETE FROM PENALTY;
DELETE FROM PARKINGLOGS;
DELETE FROM BOOKING;
DELETE FROM APPLIESTARIF;
DELETE FROM TARIFF;
DELETE FROM PARKINGBAY;
DELETE FROM PARKINGLOT;
DELETE FROM VEHICLE;
DELETE FROM USER_PHONENUMBER;
DELETE FROM USER;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. USER (20 rows)
--    UserID 1-5:  lot owners / staff
--    UserID 6-20: customers
--    referrer_id = self-referencing FK (NULL = no referrer)
-- ============================================================
INSERT INTO USER (UserID, FullName, Email, PasswordHash, CreatedAt, referrer_id) VALUES
(1,  'Nguyen Van An',     'an.nguyen@email.com',     'pbkdf2_sha256$hashed01', '2026-01-15 08:00:00', NULL),
(2,  'Tran Thi Binh',     'binh.tran@email.com',     'pbkdf2_sha256$hashed02', '2026-01-20 09:30:00', NULL),
(3,  'Le Hoang Cuong',    'cuong.le@email.com',      'pbkdf2_sha256$hashed03', '2026-02-01 10:00:00', NULL),
(4,  'Pham Minh Duc',     'duc.pham@email.com',      'pbkdf2_sha256$hashed04', '2026-02-10 11:00:00', 1),
(5,  'Vo Thi Em',         'em.vo@email.com',         'pbkdf2_sha256$hashed05', '2026-02-15 14:00:00', NULL),
(6,  'Hoang Gia Phu',     'phu.hoang@email.com',     'pbkdf2_sha256$hashed06', '2026-03-01 08:30:00', NULL),
(7,  'Do Thanh Hai',      'hai.do@email.com',        'pbkdf2_sha256$hashed07', '2026-03-05 09:00:00', 6),
(8,  'Bui Thi Huong',     'huong.bui@email.com',     'pbkdf2_sha256$hashed08', '2026-03-10 10:15:00', NULL),
(9,  'Ngo Quoc Khanh',    'khanh.ngo@email.com',     'pbkdf2_sha256$hashed09', '2026-03-15 11:00:00', 1),
(10, 'Duong Thi Lan',     'lan.duong@email.com',     'pbkdf2_sha256$hashed10', '2026-03-20 13:00:00', NULL),
(11, 'Ly Minh Nhat',      'nhat.ly@email.com',       'pbkdf2_sha256$hashed11', '2026-04-01 08:00:00', NULL),
(12, 'Phan Van Phong',    'phong.phan@email.com',    'pbkdf2_sha256$hashed12', '2026-04-05 09:30:00', 7),
(13, 'Truong Thi Quynh',  'quynh.truong@email.com',  'pbkdf2_sha256$hashed13', '2026-04-10 10:00:00', NULL),
(14, 'Dinh Hoang Son',    'son.dinh@email.com',      'pbkdf2_sha256$hashed14', '2026-04-15 11:30:00', NULL),
(15, 'Ha Thi Tuyet',      'tuyet.ha@email.com',      'pbkdf2_sha256$hashed15', '2026-05-01 08:00:00', 10),
(16, 'Mai Duc Uy',        'uy.mai@email.com',        'pbkdf2_sha256$hashed16', '2026-05-05 09:00:00', NULL),
(17, 'Chau Thi Van',      'van.chau@email.com',      'pbkdf2_sha256$hashed17', '2026-05-10 10:00:00', NULL),
(18, 'Ta Quang Vinh',     'vinh.ta@email.com',       'pbkdf2_sha256$hashed18', '2026-05-15 11:00:00', 16),
(19, 'Luong Thi Xuan',    'xuan.luong@email.com',    'pbkdf2_sha256$hashed19', '2026-06-01 08:00:00', NULL),
(20, 'Vu Dinh Yen',       'yen.vu@email.com',        'pbkdf2_sha256$hashed20', '2026-06-05 09:00:00', 8);

-- ============================================================
-- 2. USER_PHONENUMBER (20 rows)
--    Composite PK (UserID, PhoneNumber)
--    Some users have 2 phone numbers
-- ============================================================
INSERT INTO USER_PHONENUMBER (UserID, PhoneNumber) VALUES
(1,  '0901000001'),
(1,  '0901000002'),
(2,  '0912000003'),
(3,  '0933000004'),
(3,  '0933000005'),
(4,  '0944000006'),
(5,  '0955000007'),
(6,  '0966000008'),
(6,  '0966000009'),
(7,  '0977000010'),
(8,  '0988000011'),
(9,  '0909000012'),
(10, '0918000013'),
(11, '0927000014'),
(12, '0936000015'),
(13, '0945000016'),
(13, '0945000017'),
(14, '0954000018'),
(15, '0963000019'),
(16, '0972000020');

-- ============================================================
-- 3. VEHICLE (18 rows)
--    Owned by customers (UserID 6-20)
--    VehicleSize: MOTORBIKE | CAR | SUV | VAN
-- ============================================================
INSERT INTO VEHICLE (VehicleID, LicensePlate, Make, Model, Color, VehicleSize, UserID) VALUES
(1,  '51A-123.45', 'Toyota',   'Vios',        'White',  'CAR',       6),
(2,  '51A-678.90', 'Honda',    'SH 150i',     'Black',  'MOTORBIKE', 6),
(3,  '30A-111.22', 'Mazda',    'CX-5',        'Red',    'SUV',       7),
(4,  '60A-333.44', 'Honda',    'Wave Alpha',   'Blue',  'MOTORBIKE', 8),
(5,  '60A-555.66', 'Hyundai',  'Accent',      'Silver', 'CAR',       8),
(6,  '51F-777.88', 'VinFast',  'VF8',         'Blue',   'SUV',       9),
(7,  '43A-999.00', 'Toyota',   'Camry',       'Black',  'CAR',       10),
(8,  '51G-121.34', 'Honda',    'CR-V',        'White',  'SUV',       11),
(9,  '29A-565.78', 'Mazda',    'Mazda3',      'Gray',   'CAR',       12),
(10, '51H-909.12', 'Kia',      'Carnival',    'Red',    'VAN',       13),
(11, '60B-343.56', 'Hyundai',  'Tucson',      'White',  'SUV',       14),
(12, '60B-787.90', 'Toyota',   'HiAce',       'Black',  'VAN',       14),
(13, '51K-212.34', 'VinFast',  'Klara S',     'Green',  'MOTORBIKE', 15),
(14, '30B-454.56', 'Honda',    'Civic',       'Silver', 'CAR',       16),
(15, '43B-696.78', 'Mazda',    'CX-3',        'Red',    'SUV',       17),
(16, '51L-838.90', 'Kia',      'K3',          'White',  'CAR',       18),
(17, '29B-070.12', 'Yamaha',   'Exciter 155', 'Yellow', 'MOTORBIKE', 19),
(18, '51M-292.34', 'Toyota',   'Yaris',       'Gray',   'CAR',       20);

-- ============================================================
-- 4. PARKINGLOT (15 rows)
--    Owners: UserID 1-5 (3 lots each)
-- ============================================================
INSERT INTO PARKINGLOT (lot_id, LotName, Address, opening_hours, number_of_levels, UserID) VALUES
(1,  'Ben Thanh Parking',        '15 Le Loi, District 1, HCMC',              '06:00-23:00', 3, 1),
(2,  'Nguyen Hue Parking',       '88 Nguyen Hue, District 1, HCMC',          '06:00-23:00', 2, 1),
(3,  'Phu My Hung Parking',      '12 Nguyen Luong Bang, District 7, HCMC',   '00:00-23:59', 5, 1),
(4,  'Binh Thanh Parking',       '200 Xo Viet Nghe Tinh, Binh Thanh, HCMC', '06:00-22:00', 2, 2),
(5,  'Thu Duc Parking',          '55 Vo Van Ngan, Thu Duc, HCMC',            '06:00-22:00', 3, 2),
(6,  'Go Vap Parking',           '300 Quang Trung, Go Vap, HCMC',            '06:00-22:00', 1, 2),
(7,  'Bien Hoa Parking',         '10 Nguyen Ai Quoc, Bien Hoa, Dong Nai',    '06:00-21:00', 2, 3),
(8,  'Long Thanh Parking',       '5 Highway 51, Long Thanh, Dong Nai',        '06:00-21:00', 1, 3),
(9,  'Trang Bom Parking',        '22 QL1A, Trang Bom, Dong Nai',             '06:00-20:00', 1, 3),
(10, 'Son Tra Parking',          '88 Tran Phu, Son Tra, Da Nang',             '06:00-22:00', 3, 4),
(11, 'Hai Chau Parking',         '15 Bach Dang, Hai Chau, Da Nang',           '06:00-22:00', 2, 4),
(12, 'Ngu Hanh Son Parking',     '30 Le Van Hien, Ngu Hanh Son, Da Nang',     '06:00-21:00', 1, 4),
(13, 'Hoan Kiem Parking',        '5 Dinh Tien Hoang, Hoan Kiem, Hanoi',       '06:00-23:00', 4, 5),
(14, 'Cau Giay Parking',         '120 Tran Duy Hung, Cau Giay, Hanoi',        '06:00-22:00', 3, 5),
(15, 'Long Bien Parking',        '50 Ngoc Lam, Long Bien, Hanoi',             '06:00-21:00', 2, 5);

-- ============================================================
-- 5. PARKINGBAY (30 rows)
--    Composite PK (lot_id, bay_number)
--    bay_number is VARCHAR, e.g. 'A01', 'B02'
--    2 bays per lot
-- ============================================================
INSERT INTO PARKINGBAY (lot_id, bay_number, bay_status, bay_size, bay_type) VALUES
-- Lot 1
(1,  'A01', 'OCCUPIED',    'MEDIUM', 'STANDARD'),
(1,  'A02', 'AVAILABLE',   'LARGE',  'VIP'),
-- Lot 2
(2,  'A01', 'AVAILABLE',   'MEDIUM', 'STANDARD'),
(2,  'A02', 'RESERVED',    'MEDIUM', 'STANDARD'),
-- Lot 3
(3,  'A01', 'AVAILABLE',   'LARGE',  'STANDARD'),
(3,  'A02', 'OCCUPIED',    'SMALL',  'EV_CHARGING'),
-- Lot 4
(4,  'A01', 'AVAILABLE',   'LARGE',  'VIP'),
(4,  'A02', 'MAINTENANCE', 'MEDIUM', 'STANDARD'),
-- Lot 5
(5,  'A01', 'OCCUPIED',    'MEDIUM', 'STANDARD'),
(5,  'A02', 'AVAILABLE',   'LARGE',  'DISABLED'),
-- Lot 6
(6,  'A01', 'AVAILABLE',   'LARGE',  'STANDARD'),
(6,  'A02', 'RESERVED',    'MEDIUM', 'VIP'),
-- Lot 7
(7,  'A01', 'AVAILABLE',   'MEDIUM', 'STANDARD'),
(7,  'A02', 'AVAILABLE',   'SMALL',  'STANDARD'),
-- Lot 8
(8,  'A01', 'OCCUPIED',    'LARGE',  'STANDARD'),
(8,  'A02', 'AVAILABLE',   'MEDIUM', 'EV_CHARGING'),
-- Lot 9
(9,  'A01', 'AVAILABLE',   'LARGE',  'VIP'),
(9,  'A02', 'OCCUPIED',    'MEDIUM', 'STANDARD'),
-- Lot 10
(10, 'A01', 'AVAILABLE',   'MEDIUM', 'STANDARD'),
(10, 'A02', 'AVAILABLE',   'SMALL',  'DISABLED'),
-- Lot 11
(11, 'A01', 'RESERVED',    'LARGE',  'STANDARD'),
(11, 'A02', 'OCCUPIED',    'MEDIUM', 'STANDARD'),
-- Lot 12
(12, 'A01', 'AVAILABLE',   'MEDIUM', 'VIP'),
(12, 'A02', 'MAINTENANCE', 'LARGE',  'STANDARD'),
-- Lot 13
(13, 'A01', 'AVAILABLE',   'MEDIUM', 'STANDARD'),
(13, 'A02', 'AVAILABLE',   'LARGE',  'EV_CHARGING'),
-- Lot 14
(14, 'A01', 'OCCUPIED',    'MEDIUM', 'STANDARD'),
(14, 'A02', 'AVAILABLE',   'SMALL',  'STANDARD'),
-- Lot 15
(15, 'A01', 'AVAILABLE',   'LARGE',  'STANDARD'),
(15, 'A02', 'RESERVED',    'MEDIUM', 'VIP');

-- ============================================================
-- 6. TARIFF (15 rows)
--    day_of_week: MONDAY-SUNDAY | ALL
-- ============================================================
INSERT INTO TARIFF (Tariff_ID, day_of_week, start_hour, end_hour, hourly_rate) VALUES
(1,  'MONDAY',    '06:00', '12:00', 15000.00),
(2,  'MONDAY',    '12:00', '18:00', 20000.00),
(3,  'MONDAY',    '18:00', '23:00', 10000.00),
(4,  'TUESDAY',   '06:00', '12:00', 15000.00),
(5,  'TUESDAY',   '12:00', '23:00', 20000.00),
(6,  'WEDNESDAY', '06:00', '12:00', 15000.00),
(7,  'WEDNESDAY', '12:00', '23:00', 18000.00),
(8,  'THURSDAY',  '06:00', '18:00', 17000.00),
(9,  'THURSDAY',  '18:00', '23:00', 12000.00),
(10, 'FRIDAY',    '06:00', '12:00', 18000.00),
(11, 'FRIDAY',    '12:00', '23:00', 25000.00),
(12, 'SATURDAY',  '06:00', '12:00', 22000.00),
(13, 'SATURDAY',  '12:00', '23:00', 28000.00),
(14, 'SUNDAY',    '06:00', '12:00', 20000.00),
(15, 'ALL',       '06:00', '23:00', 16000.00);

-- ============================================================
-- 7. APPLIESTARIF (15 rows)
--    Junction table: which lot applies which tariff
--    NOTE: table name is APPLIESTARIF (one F) per DDL
-- ============================================================
INSERT INTO APPLIESTARIF (lot_id, Tariff_ID, effective_date) VALUES
(1,  1,  '2026-01-01'),
(1,  2,  '2026-01-01'),
(2,  3,  '2026-01-01'),
(3,  4,  '2026-02-01'),
(3,  5,  '2026-02-01'),
(4,  6,  '2026-02-01'),
(5,  7,  '2026-03-01'),
(6,  8,  '2026-03-01'),
(7,  9,  '2026-03-01'),
(8,  10, '2026-04-01'),
(9,  11, '2026-04-01'),
(10, 12, '2026-05-01'),
(11, 13, '2026-05-01'),
(12, 14, '2026-06-01'),
(13, 15, '2026-06-01');

-- ============================================================
-- 8. BOOKING (35 rows)
--    BookingStatus: PENDING | CONFIRMED | COMPLETED | CANCELLED | EXPIRED
--    FK to PARKINGBAY is composite (lot_id, bay_number)
--    Distribution:
--      BookingID  1-15 : CANCELLED -> has Cancellation, no Payment
--      BookingID 16-30 : COMPLETED -> has Payment (SUCCESS)
--      BookingID 31-33 : CONFIRMED -> active, currently parked
--      BookingID 34    : PENDING   -> awaiting confirmation
--      BookingID 35    : EXPIRED   -> never confirmed in time
-- ============================================================
INSERT INTO BOOKING (BookingID, StartTime, EndTime, TotalAmount, BookingStatus, created_at, UserID, VehicleID, lot_id, bay_number) VALUES
-- === CANCELLED (1-15) ===
(1,  '2026-10-05 08:00:00', '2026-10-05 12:00:00',  60000.00, 'CANCELLED', '2026-10-01 10:00:00', 6,  1,  1,  'A01'),
(2,  '2026-10-06 09:00:00', '2026-10-06 13:00:00',  80000.00, 'CANCELLED', '2026-10-02 11:00:00', 7,  3,  2,  'A02'),
(3,  '2026-10-07 10:00:00', '2026-10-07 14:00:00',  60000.00, 'CANCELLED', '2026-10-03 09:00:00', 8,  4,  3,  'A02'),
(4,  '2026-10-08 08:00:00', '2026-10-08 11:00:00',  45000.00, 'CANCELLED', '2026-10-04 10:00:00', 9,  6,  4,  'A02'),
(5,  '2026-10-09 09:00:00', '2026-10-09 12:00:00',  60000.00, 'CANCELLED', '2026-10-05 14:00:00', 10, 7,  5,  'A02'),
(6,  '2026-10-10 10:00:00', '2026-10-10 15:00:00', 100000.00, 'CANCELLED', '2026-10-06 08:00:00', 11, 8,  6,  'A02'),
(7,  '2026-10-11 08:00:00', '2026-10-11 12:00:00',  80000.00, 'CANCELLED', '2026-10-07 15:00:00', 12, 9,  7,  'A02'),
(8,  '2026-10-12 09:00:00', '2026-10-12 13:00:00',  60000.00, 'CANCELLED', '2026-10-08 11:00:00', 13, 10, 8,  'A02'),
(9,  '2026-10-13 10:00:00', '2026-10-13 14:00:00',  80000.00, 'CANCELLED', '2026-10-09 09:00:00', 14, 11, 9,  'A02'),
(10, '2026-10-14 08:00:00', '2026-10-14 11:00:00',  45000.00, 'CANCELLED', '2026-10-10 13:00:00', 15, 13, 10, 'A02'),
(11, '2026-10-15 09:00:00', '2026-10-15 12:00:00',  60000.00, 'CANCELLED', '2026-10-11 10:00:00', 16, 14, 11, 'A02'),
(12, '2026-10-16 10:00:00', '2026-10-16 14:00:00',  80000.00, 'CANCELLED', '2026-10-12 16:00:00', 17, 15, 12, 'A02'),
(13, '2026-10-17 08:00:00', '2026-10-17 12:00:00',  60000.00, 'CANCELLED', '2026-10-13 08:00:00', 18, 16, 13, 'A02'),
(14, '2026-10-18 09:00:00', '2026-10-18 13:00:00',  80000.00, 'CANCELLED', '2026-10-14 12:00:00', 19, 17, 14, 'A02'),
(15, '2026-10-19 10:00:00', '2026-10-19 14:00:00',  60000.00, 'CANCELLED', '2026-10-15 09:00:00', 20, 18, 15, 'A02'),
-- === COMPLETED (16-30) ===
(16, '2026-10-20 08:00:00', '2026-10-20 12:00:00',  60000.00, 'COMPLETED', '2026-10-18 10:00:00', 6,  1,  1,  'A02'),
(17, '2026-10-21 09:00:00', '2026-10-21 13:00:00',  80000.00, 'COMPLETED', '2026-10-19 11:00:00', 7,  3,  2,  'A01'),
(18, '2026-10-22 10:00:00', '2026-10-22 14:00:00',  80000.00, 'COMPLETED', '2026-10-20 09:00:00', 8,  5,  3,  'A01'),
(19, '2026-10-23 08:00:00', '2026-10-23 11:00:00',  45000.00, 'COMPLETED', '2026-10-21 10:00:00', 9,  6,  4,  'A01'),
(20, '2026-10-24 09:00:00', '2026-10-24 12:00:00',  60000.00, 'COMPLETED', '2026-10-22 14:00:00', 10, 7,  5,  'A01'),
(21, '2026-10-25 10:00:00', '2026-10-25 15:00:00', 100000.00, 'COMPLETED', '2026-10-23 08:00:00', 11, 8,  6,  'A01'),
(22, '2026-10-26 08:00:00', '2026-10-26 12:00:00',  80000.00, 'COMPLETED', '2026-10-24 15:00:00', 12, 9,  7,  'A01'),
(23, '2026-10-27 09:00:00', '2026-10-27 13:00:00',  60000.00, 'COMPLETED', '2026-10-25 11:00:00', 13, 10, 8,  'A01'),
(24, '2026-10-28 10:00:00', '2026-10-28 14:00:00',  80000.00, 'COMPLETED', '2026-10-26 09:00:00', 14, 12, 9,  'A01'),
(25, '2026-10-29 08:00:00', '2026-10-29 11:00:00',  45000.00, 'COMPLETED', '2026-10-27 13:00:00', 15, 13, 10, 'A01'),
(26, '2026-10-30 09:00:00', '2026-10-30 12:00:00',  60000.00, 'COMPLETED', '2026-10-28 10:00:00', 16, 14, 11, 'A01'),
(27, '2026-10-31 10:00:00', '2026-10-31 15:00:00', 100000.00, 'COMPLETED', '2026-10-29 16:00:00', 17, 15, 12, 'A01'),
(28, '2026-11-01 08:00:00', '2026-11-01 12:00:00',  80000.00, 'COMPLETED', '2026-10-30 08:00:00', 18, 16, 13, 'A01'),
(29, '2026-11-02 09:00:00', '2026-11-02 13:00:00',  60000.00, 'COMPLETED', '2026-10-31 12:00:00', 19, 17, 14, 'A01'),
(30, '2026-11-03 10:00:00', '2026-11-03 14:00:00',  80000.00, 'COMPLETED', '2026-11-01 09:00:00', 20, 18, 15, 'A01'),
-- === CONFIRMED (31-33) — currently parked ===
(31, '2026-11-16 08:00:00', '2026-11-16 14:00:00',  90000.00, 'CONFIRMED', '2026-11-15 20:00:00', 6,  1,  1,  'A01'),
(32, '2026-11-16 09:00:00', '2026-11-16 13:00:00',  60000.00, 'CONFIRMED', '2026-11-15 21:00:00', 8,  4,  3,  'A02'),
(33, '2026-11-16 10:00:00', '2026-11-16 15:00:00',  75000.00, 'CONFIRMED', '2026-11-15 22:00:00', 10, 7,  5,  'A01'),
-- === PENDING (34) — awaiting confirmation ===
(34, '2026-11-17 07:00:00', '2026-11-17 12:00:00',  75000.00, 'PENDING',   '2026-11-16 19:00:00', 14, 11, 9,  'A02'),
-- === EXPIRED (35) — never confirmed in time ===
(35, '2026-11-10 08:30:00', '2026-11-10 13:00:00',  67500.00, 'EXPIRED',   '2026-11-08 23:00:00', 16, 14, 11, 'A02');

-- ============================================================
-- 9. PARKINGLOGS (20 rows)
--    NOTE: This table has NO BookingID column per DDL
--    Tracks barrier entry/exit by vehicle at a lot
--    LogID 1-15:  completed -> check_in + check_out (some overstay)
--    LogID 16-20: active -> check_in only, check_out = NULL
--    UserID (optional) = staff who verified
-- ============================================================
INSERT INTO PARKINGLOGS (log_id, check_in_time, check_out_time, lot_id, UserID, VehicleID) VALUES
-- Completed – checked out, with overstay
(1,  '2026-10-20 08:05:00', '2026-10-20 12:45:00', 1,  1,    1),
(2,  '2026-10-21 09:10:00', '2026-10-21 13:30:00', 2,  2,    3),
(3,  '2026-10-22 10:03:00', '2026-10-22 15:00:00', 3,  3,    5),
(4,  '2026-10-23 08:07:00', '2026-10-23 11:20:00', 4,  NULL,  6),
(5,  '2026-10-24 09:02:00', '2026-10-24 12:50:00', 5,  1,    7),
(6,  '2026-10-25 10:05:00', '2026-10-25 15:40:00', 6,  2,    8),
(7,  '2026-10-26 08:08:00', '2026-10-26 12:35:00', 7,  NULL,  9),
(8,  '2026-10-27 09:03:00', '2026-10-27 13:25:00', 8,  3,    10),
(9,  '2026-10-28 10:06:00', '2026-10-28 14:55:00', 9,  4,    12),
(10, '2026-10-29 08:04:00', '2026-10-29 11:15:00', 10, NULL,  13),
(11, '2026-10-30 09:07:00', '2026-10-30 13:10:00', 11, 5,    14),
(12, '2026-10-31 10:02:00', '2026-10-31 15:45:00', 12, 4,    15),
(13, '2026-11-01 08:05:00', '2026-11-01 12:30:00', 13, 5,    16),
(14, '2026-11-02 09:08:00', '2026-11-02 13:20:00', 14, NULL,  17),
(15, '2026-11-03 10:04:00', '2026-11-03 14:40:00', 15, 1,    18),
-- Active – currently parked, not checked out yet
(16, '2026-11-16 08:05:00', NULL,                   1,  NULL,  1),
(17, '2026-11-16 09:03:00', NULL,                   3,  3,    4),
(18, '2026-11-16 10:02:00', NULL,                   5,  NULL,  7),
(19, '2026-11-16 07:05:00', NULL,                   9,  4,    11),
(20, '2026-11-16 08:35:00', NULL,                   11, 5,    14);

-- ============================================================
-- 10. PENALTY (15 rows)
--     Each penalty linked to a ParkingLog with overstay
--     penalty_amount ~ overstay_minutes x 500 VND
--     payment_status: UNPAID | PAID | WAIVED
-- ============================================================
INSERT INTO PENALTY (PenaltyID, overstay_minutes, penalty_amount, issued_at, payment_status, log_id) VALUES
(1,  45, 22500.00,  '2026-10-20 12:46:00', 'PAID',   1),
(2,  30, 15000.00,  '2026-10-21 13:31:00', 'PAID',   2),
(3,  60, 30000.00,  '2026-10-22 15:01:00', 'PAID',   3),
(4,  20, 10000.00,  '2026-10-23 11:21:00', 'UNPAID', 4),
(5,  50, 25000.00,  '2026-10-24 12:51:00', 'PAID',   5),
(6,  40, 20000.00,  '2026-10-25 15:41:00', 'PAID',   6),
(7,  35, 17500.00,  '2026-10-26 12:36:00', 'WAIVED', 7),
(8,  25, 12500.00,  '2026-10-27 13:26:00', 'PAID',   8),
(9,  55, 27500.00,  '2026-10-28 14:56:00', 'UNPAID', 9),
(10, 15,  7500.00,  '2026-10-29 11:16:00', 'PAID',   10),
(11, 70, 35000.00,  '2026-10-30 13:11:00', 'UNPAID', 11),
(12, 45, 22500.00,  '2026-10-31 15:46:00', 'PAID',   12),
(13, 30, 15000.00,  '2026-11-01 12:31:00', 'PAID',   13),
(14, 20, 10000.00,  '2026-11-02 13:21:00', 'WAIVED', 14),
(15, 40, 20000.00,  '2026-11-03 14:41:00', 'UNPAID', 15);

-- ============================================================
-- 11. PAYMENT (18 rows)
--     PaymentID 1-15:  SUCCESS (for completed bookings 16-30)
--     PaymentID 16-18: PENDING (for confirmed bookings 31-33)
--     PaymentMethod: CREDIT_CARD | DEBIT_CARD | E_WALLET | BANK_TRANSFER | CASH
--     PaymentStatus: PENDING | SUCCESS | FAILED | REFUNDED
-- ============================================================
INSERT INTO PAYMENT (PaymentID, AmountPaid, PaymentMethod, TransactionID, PaymentStatus, paid_at, BookingID) VALUES
(1,  60000.00,  'CREDIT_CARD',   'TXN20261020001', 'SUCCESS',  '2026-10-20 12:50:00', 16),
(2,  80000.00,  'E_WALLET',      'TXN20261021001', 'SUCCESS',  '2026-10-21 13:35:00', 17),
(3,  80000.00,  'CASH',          'TXN20261022001', 'SUCCESS',  '2026-10-22 15:05:00', 18),
(4,  45000.00,  'BANK_TRANSFER', 'TXN20261023001', 'SUCCESS',  '2026-10-23 11:25:00', 19),
(5,  60000.00,  'DEBIT_CARD',    'TXN20261024001', 'SUCCESS',  '2026-10-24 12:55:00', 20),
(6,  100000.00, 'E_WALLET',      'TXN20261025001', 'SUCCESS',  '2026-10-25 15:45:00', 21),
(7,  80000.00,  'CASH',          'TXN20261026001', 'SUCCESS',  '2026-10-26 12:40:00', 22),
(8,  60000.00,  'CREDIT_CARD',   'TXN20261027001', 'SUCCESS',  '2026-10-27 13:30:00', 23),
(9,  80000.00,  'BANK_TRANSFER', 'TXN20261028001', 'SUCCESS',  '2026-10-28 15:00:00', 24),
(10, 45000.00,  'E_WALLET',      'TXN20261029001', 'SUCCESS',  '2026-10-29 11:20:00', 25),
(11, 60000.00,  'DEBIT_CARD',    'TXN20261030001', 'SUCCESS',  '2026-10-30 13:15:00', 26),
(12, 100000.00, 'CASH',          'TXN20261031001', 'SUCCESS',  '2026-10-31 15:50:00', 27),
(13, 80000.00,  'E_WALLET',      'TXN20261101001', 'SUCCESS',  '2026-11-01 12:35:00', 28),
(14, 60000.00,  'BANK_TRANSFER', 'TXN20261102001', 'SUCCESS',  '2026-11-02 13:25:00', 29),
(15, 80000.00,  'CREDIT_CARD',   'TXN20261103001', 'SUCCESS',  '2026-11-03 14:45:00', 30),
-- Pending – confirmed bookings
(16, 90000.00,  'E_WALLET',      'TXN20261116001', 'PENDING',  '2026-11-16 08:00:00', 31),
(17, 60000.00,  'CREDIT_CARD',   'TXN20261116002', 'PENDING',  '2026-11-16 09:00:00', 32),
(18, 75000.00,  'BANK_TRANSFER', 'TXN20261116003', 'PENDING',  '2026-11-16 10:00:00', 33);

-- ============================================================
-- 12. CANCELLATION (15 rows)
--     Each linked to a cancelled booking (BookingID 1-15)
--     NOTE: column is "canceled_at" (one L) per DDL
--     reason is NOT NULL per DDL
--     refund_amount: full / partial / none (0)
-- ============================================================
INSERT INTO CANCELLATION (CancellationID, canceled_at, reason, refund_amount, BookingID) VALUES
(1,  '2026-10-03 10:00:00', 'Change of plans',                 60000.00, 1),
(2,  '2026-10-04 11:00:00', 'Unexpected work commitment',      80000.00, 2),
(3,  '2026-10-05 09:00:00', 'Vehicle breakdown',               60000.00, 3),
(4,  '2026-10-06 10:00:00', 'Switched to a closer lot',        22500.00, 4),
(5,  '2026-10-07 14:00:00', 'Personal reasons',                60000.00, 5),
(6,  '2026-10-08 08:00:00', 'Schedule conflict',               50000.00, 6),
(7,  '2026-10-09 15:00:00', 'Trip cancelled',                  80000.00, 7),
(8,  '2026-10-10 11:00:00', 'Late cancellation - no refund',       0.00, 8),
(9,  '2026-10-11 09:00:00', 'Booked wrong time slot',          80000.00, 9),
(10, '2026-10-12 13:00:00', 'Lot too far away',                45000.00, 10),
(11, '2026-10-13 10:00:00', 'Bad weather',                     60000.00, 11),
(12, '2026-10-14 16:00:00', 'Found free parking',              40000.00, 12),
(13, '2026-10-15 08:00:00', 'No longer need parking',          60000.00, 13),
(14, '2026-10-16 12:00:00', 'Itinerary change',                80000.00, 14),
(15, '2026-10-17 09:00:00', 'Cancelled without specific reason',   0.00, 15);

-- ============================================================
-- ROW COUNT SUMMARY
-- ============================================================
-- | Table              | Rows    |
-- |--------------------|---------|
-- | USER               |      20 |
-- | USER_PHONENUMBER   |      20 |
-- | VEHICLE            |      18 |
-- | PARKINGLOT         |      15 |
-- | PARKINGBAY         |      30 |
-- | TARIFF             |      15 |
-- | APPLIESTARIF       |      15 |
-- | BOOKING            |      35 |
-- | PARKINGLOGS        |      20 |
-- | PENALTY            |      15 |
-- | PAYMENT            |      18 |
-- | CANCELLATION       |      15 |
-- |--------------------|---------|
-- | TOTAL              |     236 |

-- ============================================================
-- SCENARIOS COVERED
-- ============================================================
-- 1.  Booking CANCELLED (15) – has Cancellation, no Payment
-- 2.  Booking COMPLETED (15) – has Payment (SUCCESS)
-- 3.  Booking CONFIRMED (3)  – currently parked, Payment PENDING
-- 4.  Booking PENDING   (1)  – awaiting confirmation
-- 5.  Booking EXPIRED   (1)  – never confirmed in time
-- 6.  Overstay / Penalty: 15 cases (15-70 min), status PAID/UNPAID/WAIVED
-- 7.  Payment methods: CREDIT_CARD, DEBIT_CARD, E_WALLET, BANK_TRANSFER, CASH
-- 8.  Payment status: SUCCESS (15) + PENDING (3)
-- 9.  Cancellation refund: full, partial, and no refund (0 VND)
-- 10. Vehicle sizes: MOTORBIKE, CAR, SUV, VAN
-- 11. Bay types: STANDARD, VIP, DISABLED, EV_CHARGING
-- 12. Bay statuses: AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE
-- 13. Self-referral: 7 users with a referrer (referrer_id NOT NULL)
-- 14. Staff verify: some ParkingLogs have UserID (staff), some NULL
-- 15. User roles: lot owners (1-5) vs customers (6-20)
-- 16. Multi-ownership: each owner has 3 lots; some customers have 2 vehicles
-- 17. Data spans Jan -> Nov 2026 (diverse timestamps)
-- 18. All 5 BookingStatus values used: PENDING, CONFIRMED, COMPLETED, CANCELLED, EXPIRED