package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Cancellation;

public class CancellationDAO {

    // Get all cancellations
    public List<Cancellation> getAllCancellations() throws SQLException {
        String sql = "SELECT * FROM CANCELLATION";
        List<Cancellation> cancellations = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cancellations.add(mapResultSetToCancellation(rs));
            }
        }

        return cancellations;
    }

    // Find cancellation by ID
    public Cancellation getCancellationById(int cancellationID)
            throws SQLException {

        String sql = "SELECT * FROM CANCELLATION WHERE CancellationID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cancellationID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCancellation(rs);
                }
            }
        }

        return null;
    }

    // Find cancellation by booking ID
    public Cancellation getCancellationByBookingId(int bookingID)
            throws SQLException {

        String sql = "SELECT * FROM CANCELLATION WHERE BookingID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCancellation(rs);
                }
            }
        }

        return null;
    }

    // Insert a new cancellation
    public int insertCancellation(Cancellation cancellation)
            throws SQLException {

        String sql = "INSERT INTO CANCELLATION "
                + "(reason, refund_amount, BookingID) "
                + "VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cancellation.getReason());
            ps.setBigDecimal(2, cancellation.getRefundAmount());
            ps.setInt(3, cancellation.getBookingID());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    // Update cancellation information
    public boolean updateCancellation(Cancellation cancellation)
            throws SQLException {

        String sql = "UPDATE CANCELLATION SET "
                + "reason = ?, refund_amount = ?, BookingID = ? "
                + "WHERE CancellationID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cancellation.getReason());
            ps.setBigDecimal(2, cancellation.getRefundAmount());
            ps.setInt(3, cancellation.getBookingID());
            ps.setInt(4, cancellation.getCancellationID());

            return ps.executeUpdate() > 0;
        }
    }

    // Delete cancellation
    public boolean deleteCancellation(int cancellationID)
            throws SQLException {

        String sql = "DELETE FROM CANCELLATION WHERE CancellationID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cancellationID);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into Cancellation object
    private Cancellation mapResultSetToCancellation(ResultSet rs)
            throws SQLException {

        Cancellation cancellation = new Cancellation();

        cancellation.setCancellationID(rs.getInt("CancellationID"));
        cancellation.setCanceledAt(rs.getTimestamp("canceled_at"));
        cancellation.setReason(rs.getString("reason"));
        cancellation.setRefundAmount(rs.getBigDecimal("refund_amount"));
        cancellation.setBookingID(rs.getInt("BookingID"));

        return cancellation;
    }
}
