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

    // Insert booking
    public boolean insertBooking(Booking booking) throws SQLException {
        String sql = "INSERT INTO BOOKING "
                + "(StartTime, EndTime, TotalAmount, BookingStatus, "
                + "UserID, VehicleID, lot_id, bay_number) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, booking.getStartTime());
            ps.setTimestamp(2, booking.getEndTime());
            ps.setBigDecimal(3, booking.getTotalAmount());
            ps.setString(4, booking.getBookingStatus());
            ps.setInt(5, booking.getUserID());
            ps.setInt(6, booking.getVehicleID());
            ps.setInt(7, booking.getLotId());
            ps.setString(8, booking.getBayNumber());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        booking.setBookingID(keys.getInt(1));
                    }
                }
                return true;
            }
        }

        return false;
    }

    // Update booking
    public boolean updateBooking(Booking booking) throws SQLException {
        String sql = "UPDATE BOOKING SET "
                + "StartTime = ?, EndTime = ?, TotalAmount = ?, "
                + "BookingStatus = ?, UserID = ?, VehicleID = ?, "
                + "lot_id = ?, bay_number = ? "
                + "WHERE BookingID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, booking.getStartTime());
            ps.setTimestamp(2, booking.getEndTime());
            ps.setBigDecimal(3, booking.getTotalAmount());
            ps.setString(4, booking.getBookingStatus());
            ps.setInt(5, booking.getUserID());
            ps.setInt(6, booking.getVehicleID());
            ps.setInt(7, booking.getLotId());
            ps.setString(8, booking.getBayNumber());
            ps.setInt(9, booking.getBookingID());

            return ps.executeUpdate() > 0;
        }
    }

    // Update booking status
    public boolean updateBookingStatus(int bookingID, String status)
            throws SQLException {

        String sql = "UPDATE BOOKING SET BookingStatus = ? "
                + "WHERE BookingID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, bookingID);

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
