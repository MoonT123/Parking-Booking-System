package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Booking;

public class BookingDAO {

    // Get all bookings
    public List<Booking> getAllBookings() throws SQLException {
        String sql = "SELECT * FROM BOOKING";
        List<Booking> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToBooking(rs));
            }
        }

        return list;
    }

    // Get booking by ID
    public Booking getBookingById(int bookingID) throws SQLException {
        String sql = "SELECT * FROM BOOKING WHERE BookingID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBooking(rs);
                }
            }
        }

        return null;
    }

    // Get bookings by user ID
    public List<Booking> getBookingsByUserId(int userID)
            throws SQLException {

        String sql = "SELECT * FROM BOOKING WHERE UserID = ?";
        List<Booking> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBooking(rs));
                }
            }
        }

        return list;
    }

    // Get bookings by status
    public List<Booking> getBookingsByStatus(String status)
            throws SQLException {

        String sql = "SELECT * FROM BOOKING WHERE BookingStatus = ?";
        List<Booking> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToBooking(rs));
                }
            }
        }

        return list;
    }

    public boolean insertBooking(Booking booking) throws SQLException {
        return saveBooking(booking, false);
    }

    public boolean updateBooking(Booking booking) throws SQLException {
        return saveBooking(booking, true);
    }

    private boolean saveBooking(Booking booking, boolean update) throws SQLException {
        String sql = update
                ? """
                  UPDATE BOOKING SET StartTime = ?, EndTime = ?, TotalAmount = ?,
                      UserID = ?, VehicleID = ?, lot_id = ?, bay_number = ?
                  WHERE BookingID = ? AND BookingStatus IN ('PENDING', 'CONFIRMED')
                  """
                : """
                  INSERT INTO BOOKING (StartTime, EndTime, TotalAmount, UserID,
                                       VehicleID, lot_id, bay_number)
                  VALUES (?, ?, ?, ?, ?, ?, ?)
                  """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conn.setAutoCommit(false);
            try {
                lockCompatibleBay(conn, booking);
                int excludedId = update ? booking.getBookingID() : 0;
                if (hasOverlappingBooking(conn, booking.getLotId(), booking.getBayNumber(),
                        booking.getStartTime(), booking.getEndTime(), excludedId)) {
                    throw new IllegalArgumentException("This parking bay is already booked during the selected time.");
                }

                java.math.BigDecimal total = BookingPricing.total(conn, booking);
                int bookingId = booking.getBookingID();
                try (PreparedStatement ps = conn.prepareStatement(sql, update
                        ? PreparedStatement.NO_GENERATED_KEYS : PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, booking.getStartTime());
                    ps.setTimestamp(2, booking.getEndTime());
                    ps.setBigDecimal(3, total);
                    ps.setInt(4, booking.getUserID());
                    ps.setInt(5, booking.getVehicleID());
                    ps.setInt(6, booking.getLotId());
                    ps.setString(7, booking.getBayNumber());
                    if (update) {
                        ps.setInt(8, bookingId);
                    }
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                    if (!update) {
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException("Missing generated booking ID.");
                            }
                            bookingId = keys.getInt(1);
                        }
                    }
                }
                conn.commit();
                booking.setBookingID(bookingId);
                booking.setTotalAmount(total);
                return true;
            } catch (SQLException | RuntimeException ex) {
                rollback(conn, ex);
                throw ex;
            }
        }
    }

    private void lockCompatibleBay(Connection conn, Booking booking) throws SQLException {
        String sql = """
                SELECT pb.bay_size, pb.bay_status
                FROM PARKINGBAY pb
                WHERE pb.lot_id = ? AND pb.bay_number = ?
                FOR UPDATE
                """;
        String baySize;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booking.getLotId());
            ps.setString(2, booking.getBayNumber());
            try (ResultSet rows = ps.executeQuery()) {
                if (!rows.next() || "MAINTENANCE".equals(rows.getString("bay_status"))) {
                    throw new IllegalArgumentException("This parking bay is missing or under maintenance.");
                }
                baySize = rows.getString("bay_size");
            }
        }
        String fitSql = """
                SELECT 1 FROM VEHICLE v JOIN VEHICLE_BAY_SIZE fit
                    ON fit.vehicle_size = v.VehicleSize AND fit.bay_size = ?
                WHERE v.VehicleID = ? AND v.UserID = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(fitSql)) {
            ps.setString(1, baySize);
            ps.setInt(2, booking.getVehicleID());
            ps.setInt(3, booking.getUserID());
            try (ResultSet rows = ps.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalArgumentException("Vehicle does not belong to the user or does not fit the bay.");
                }
            }
        }
    }

    public boolean cancelBooking(int bookingID) throws SQLException {
        String update = """
                UPDATE BOOKING SET BookingStatus = 'CANCELLED'
                WHERE BookingID = ? AND BookingStatus IN ('PENDING', 'CONFIRMED')
                """;
        String audit = """
                INSERT INTO CANCELLATION (BookingID, reason)
                VALUES (?, 'Cancelled from booking management')
                """;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement status = conn.prepareStatement(update);
                 PreparedStatement cancellation = conn.prepareStatement(audit)) {
                status.setInt(1, bookingID);
                if (status.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
                cancellation.setInt(1, bookingID);
                cancellation.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                rollback(conn, ex);
                throw ex;
            }
        }
    }

    private static void rollback(Connection conn, Exception failure) {
        try {
            conn.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }

    public boolean updateBookingStatus(int bookingID, String status) throws SQLException {
        if ("CANCELLED".equals(status)) {
            return cancelBooking(bookingID);
        }
        String sql = """
                UPDATE BOOKING SET BookingStatus = ?
                WHERE BookingID = ?
                  AND ((BookingStatus = 'PENDING' AND ? IN ('PENDING', 'CONFIRMED', 'EXPIRED'))
                    OR (BookingStatus = 'CONFIRMED' AND ? IN ('CONFIRMED', 'COMPLETED', 'EXPIRED')))
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookingID);
            ps.setString(3, status);
            ps.setString(4, status);
            return ps.executeUpdate() > 0;
        }
    }

    // Delete booking
    public boolean deleteBooking(int bookingID) throws SQLException {
        String sql = "DELETE FROM BOOKING WHERE BookingID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingID);

            return ps.executeUpdate() > 0;
        }
    }

    // Check overlapping bookings
    public boolean hasOverlappingBooking(
            int lotId,
            String bayNumber,
            java.sql.Timestamp startTime,
            java.sql.Timestamp endTime,
            int excludeBookingId) throws SQLException {

        try (Connection conn = DBConnection.getConnection()) {
            return hasOverlappingBooking(conn, lotId, bayNumber, startTime, endTime, excludeBookingId);
        }
    }

    private boolean hasOverlappingBooking(Connection conn, int lotId, String bayNumber,
            java.sql.Timestamp startTime, java.sql.Timestamp endTime, int excludeBookingId)
            throws SQLException {
        String sql = """
                SELECT 1 FROM BOOKING
                WHERE lot_id = ? AND bay_number = ? AND BookingID <> ?
                  AND BookingStatus IN ('PENDING', 'CONFIRMED')
                  AND StartTime < ? AND EndTime > ?
                LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lotId);
            ps.setString(2, bayNumber);
            ps.setInt(3, excludeBookingId);
            ps.setTimestamp(4, endTime);
            ps.setTimestamp(5, startTime);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Convert ResultSet into Booking object
    private Booking mapResultSetToBooking(ResultSet rs)
            throws SQLException {

        Booking booking = new Booking();

        booking.setBookingID(rs.getInt("BookingID"));
        booking.setStartTime(rs.getTimestamp("StartTime"));
        booking.setEndTime(rs.getTimestamp("EndTime"));
        booking.setTotalAmount(rs.getBigDecimal("TotalAmount"));
        booking.setBookingStatus(rs.getString("BookingStatus"));
        booking.setCreatedAt(rs.getTimestamp("created_at"));
        booking.setUserID(rs.getInt("UserID"));
        booking.setVehicleID(rs.getInt("VehicleID"));
        booking.setLotId(rs.getInt("lot_id"));
        booking.setBayNumber(rs.getString("bay_number"));

        return booking;
    }
}
