-- Parking Booking System: 12 queries aligned to Functional_Dependencies.pdf
-- MySQL 8.0+. Run USE with the actual database name if different.
USE ParkingBookingSystem;
SET @lot_id = 1;
SET @customer_id = 6; -- Sample customer with bookings
SET @report_date = DATE('2026-11-16'); -- Date with CONFIRMED sample bookings
-- Verified against 01_create_database.sql and updated 2_sample_data.sql.
-- Note: Q05, Q07 and Q09 may return no rows because sample data does not
-- contain matching scenarios. An empty result is not a SQL execution error.

-- Q01 Available bays tomorrow 14:00-16:00 (JOIN, NOT EXISTS)
SELECT pb.lot_id, pb.bay_number, pl.LotName, pb.bay_type
FROM PARKINGBAY pb JOIN PARKINGLOT pl ON pl.lot_id=pb.lot_id
WHERE pb.lot_id=@lot_id AND pb.bay_status <> 'MAINTENANCE'
AND NOT EXISTS (SELECT 1 FROM BOOKING b
 WHERE b.lot_id=pb.lot_id AND b.bay_number=pb.bay_number
 AND UPPER(b.BookingStatus) IN ('PENDING','CONFIRMED')
 AND b.StartTime < TIMESTAMP(CURRENT_DATE + INTERVAL 1 DAY,'16:00:00')
 AND b.EndTime > TIMESTAMP(CURRENT_DATE + INTERVAL 1 DAY,'14:00:00'))
ORDER BY pb.bay_number;

-- Q02 Previous calendar month's bookings and successful payments (LEFT JOIN, GROUP BY)
SELECT b.BookingID,b.StartTime,b.EndTime,b.BookingStatus,b.TotalAmount,
 COALESCE(SUM(CASE WHEN UPPER(p.PaymentStatus)='SUCCESS' THEN p.AmountPaid ELSE 0 END),0) AS paid_amount
FROM BOOKING b LEFT JOIN PAYMENT p ON p.BookingID=b.BookingID
WHERE b.UserID=@customer_id
 AND b.StartTime >= DATE_FORMAT(CURRENT_DATE - INTERVAL 1 MONTH,'%Y-%m-01')
 AND b.StartTime < DATE_FORMAT(CURRENT_DATE,'%Y-%m-01')
GROUP BY b.BookingID,b.StartTime,b.EndTime,b.BookingStatus,b.TotalAmount
ORDER BY b.StartTime DESC;

-- Q03 Hourly booked-bay utilization for selected lot/date (CTE, LEFT JOIN, GROUP BY)
WITH RECURSIVE hours AS (SELECT 0 AS hr UNION ALL SELECT hr+1 FROM hours WHERE hr<23)
SELECT h.hr AS hour_of_day,COUNT(DISTINCT pb.bay_number) AS total_bays,
 COUNT(DISTINCT CASE WHEN b.BookingID IS NOT NULL THEN pb.bay_number END) AS booked_bays,
 ROUND(100.0*COUNT(DISTINCT CASE WHEN b.BookingID IS NOT NULL THEN pb.bay_number END)
 / NULLIF(COUNT(DISTINCT pb.bay_number),0),2) AS utilization_pct
FROM hours h CROSS JOIN PARKINGBAY pb
LEFT JOIN BOOKING b ON b.lot_id=pb.lot_id AND b.bay_number=pb.bay_number
 AND UPPER(b.BookingStatus) IN ('PENDING','CONFIRMED','COMPLETED')
 AND b.StartTime < DATE_ADD(TIMESTAMP(@report_date,MAKETIME(h.hr,0,0)),INTERVAL 1 HOUR)
 AND b.EndTime > TIMESTAMP(@report_date,MAKETIME(h.hr,0,0))
WHERE pb.lot_id=@lot_id GROUP BY h.hr ORDER BY h.hr;

-- Q04 Late exits matched to a booking by same vehicle, lot and overlapping visit.
-- Logical schema has NO PARKINGLOGS.BookingID FK. Multiple plausible bookings
-- can match one log; confirm the matching business rule with database owner.
SELECT v.LicensePlate,b.BookingID,b.EndTime,l.check_out_time,
 TIMESTAMPDIFF(MINUTE,b.EndTime,l.check_out_time) AS minutes_late,
 COALESCE(SUM(p.penalty_amount),0) AS recorded_penalty_amount
FROM PARKINGLOGS l JOIN VEHICLE v ON v.VehicleID=l.VehicleID
JOIN BOOKING b ON b.VehicleID=l.VehicleID AND b.lot_id=l.lot_id
 AND b.StartTime <= l.check_out_time
 AND (l.check_in_time IS NULL OR b.EndTime >= l.check_in_time)
LEFT JOIN PENALTY p ON p.log_id=l.log_id
WHERE l.check_out_time>b.EndTime
GROUP BY v.LicensePlate,b.BookingID,b.EndTime,l.check_out_time
ORDER BY minutes_late DESC;

-- Q05 Lots where each electric bay had a booking starting last calendar week
-- The DDL uses bay_type='EV_CHARGING'.
SELECT pl.lot_id,pl.LotName FROM PARKINGLOT pl
WHERE EXISTS (SELECT 1 FROM PARKINGBAY pb WHERE pb.lot_id=pl.lot_id AND pb.bay_type='EV_CHARGING')
AND NOT EXISTS (
 SELECT 1 FROM PARKINGBAY pb WHERE pb.lot_id=pl.lot_id AND pb.bay_type='EV_CHARGING'
 AND NOT EXISTS (SELECT 1 FROM BOOKING b
 WHERE b.lot_id=pb.lot_id AND b.bay_number=pb.bay_number
 AND UPPER(b.BookingStatus) IN ('PENDING','CONFIRMED','COMPLETED')
 AND b.StartTime >= CURRENT_DATE - INTERVAL (WEEKDAY(CURRENT_DATE)+7) DAY
 AND b.StartTime < CURRENT_DATE - INTERVAL WEEKDAY(CURRENT_DATE) DAY));

-- Q06 Users with no bookings (LEFT OUTER JOIN). USER has no Role column.
SELECT u.UserID,u.FullName,u.Email FROM `USER` u
LEFT JOIN BOOKING b ON b.UserID=u.UserID
WHERE b.BookingID IS NULL ORDER BY u.UserID;

-- Q07 Lots with at least 3 confirmed bookings (GROUP BY, HAVING)
SELECT pl.lot_id,pl.LotName,COUNT(*) AS confirmed_bookings
FROM PARKINGLOT pl JOIN BOOKING b ON b.lot_id=pl.lot_id
WHERE UPPER(b.BookingStatus)='CONFIRMED'
GROUP BY pl.lot_id,pl.LotName HAVING COUNT(*)>=3
ORDER BY confirmed_bookings DESC;

-- Q08 Users with successful payments (IN)
SELECT u.UserID,u.FullName,u.Email FROM `USER` u
WHERE u.UserID IN (SELECT b.UserID FROM BOOKING b
 JOIN PAYMENT p ON p.BookingID=b.BookingID
 WHERE UPPER(p.PaymentStatus)='SUCCESS') ORDER BY u.UserID;

-- Q09 Users who booked bays in >=2 lots (GROUP BY, HAVING)
SELECT u.UserID,u.FullName,COUNT(DISTINCT b.lot_id) AS distinct_lots
FROM `USER` u JOIN BOOKING b ON b.UserID=u.UserID
GROUP BY u.UserID,u.FullName HAVING COUNT(DISTINCT b.lot_id)>=2;

-- Q10 Users who made bookings OR own parking lots (UNION)
SELECT u.UserID,u.FullName FROM `USER` u
WHERE EXISTS (SELECT 1 FROM BOOKING b WHERE b.UserID=u.UserID)
UNION
SELECT u.UserID,u.FullName FROM `USER` u
WHERE EXISTS (SELECT 1 FROM PARKINGLOT pl WHERE pl.UserID=u.UserID)
ORDER BY UserID;

-- Q11 Booking-payment summary VIEW
CREATE OR REPLACE VIEW vw_booking_payment_summary AS
SELECT b.BookingID,b.UserID,b.TotalAmount,
 COALESCE(SUM(CASE WHEN UPPER(p.PaymentStatus)='SUCCESS' THEN p.AmountPaid ELSE 0 END),0) AS successful_paid
FROM BOOKING b LEFT JOIN PAYMENT p ON p.BookingID=b.BookingID
GROUP BY b.BookingID,b.UserID,b.TotalAmount;
SELECT * FROM vw_booking_payment_summary ORDER BY BookingID;

-- Q12 Bookings not fully covered by successful payments
SELECT s.BookingID,s.UserID,s.TotalAmount,s.successful_paid,
 s.TotalAmount-s.successful_paid AS outstanding_amount
FROM vw_booking_payment_summary s
WHERE s.successful_paid<s.TotalAmount ORDER BY outstanding_amount DESC;
