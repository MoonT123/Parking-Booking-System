package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Penalty;

public class PenaltyDAO {

    // Get all penalties
    public List<Penalty> getAllPenalties() throws SQLException {
        String sql = "SELECT * FROM PENALTY";
        List<Penalty> penalties = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                penalties.add(mapResultSetToPenalty(rs));
            }
        }

        return penalties;
    }

    // Find penalty by ID
    public Penalty getPenaltyById(int penaltyID) throws SQLException {
        String sql = "SELECT * FROM PENALTY WHERE PenaltyID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, penaltyID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPenalty(rs);
                }
            }
        }

        return null;
    }

    // Get penalties by parking log ID
    public List<Penalty> getPenaltiesByLogId(int logId)
            throws SQLException {

        String sql = "SELECT * FROM PENALTY WHERE log_id = ?";
        List<Penalty> penalties = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, logId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    penalties.add(mapResultSetToPenalty(rs));
                }
            }
        }

        return penalties;
    }

    // Get penalties by payment status
    public List<Penalty> getPenaltiesByStatus(String status)
            throws SQLException {

        String sql = "SELECT * FROM PENALTY WHERE payment_status = ?";
        List<Penalty> penalties = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    penalties.add(mapResultSetToPenalty(rs));
                }
            }
        }

        return penalties;
    }

    // Insert a new penalty
    public int insertPenalty(Penalty penalty) throws SQLException {
        String sql = "INSERT INTO PENALTY "
                + "(overstay_minutes, penalty_amount, payment_status, log_id) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, penalty.getOverstayMinutes());
            ps.setBigDecimal(2, penalty.getPenaltyAmount());
            ps.setString(3, penalty.getPaymentStatus());
            ps.setInt(4, penalty.getLogId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    // Update penalty information
    public boolean updatePenalty(Penalty penalty) throws SQLException {
        String sql = "UPDATE PENALTY SET "
                + "overstay_minutes = ?, penalty_amount = ?, "
                + "payment_status = ?, log_id = ? "
                + "WHERE PenaltyID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, penalty.getOverstayMinutes());
            ps.setBigDecimal(2, penalty.getPenaltyAmount());
            ps.setString(3, penalty.getPaymentStatus());
            ps.setInt(4, penalty.getLogId());
            ps.setInt(5, penalty.getPenaltyID());

            return ps.executeUpdate() > 0;
        }
    }

    // Update penalty payment status
    public boolean updatePaymentStatus(int penaltyID, String status)
            throws SQLException {

        String sql = "UPDATE PENALTY SET payment_status = ? "
                + "WHERE PenaltyID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, penaltyID);

            return ps.executeUpdate() > 0;
        }
    }

    // Delete penalty
    public boolean deletePenalty(int penaltyID) throws SQLException {
        String sql = "DELETE FROM PENALTY WHERE PenaltyID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, penaltyID);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into Penalty object
    private Penalty mapResultSetToPenalty(ResultSet rs)
            throws SQLException {

        Penalty penalty = new Penalty();

        penalty.setPenaltyID(rs.getInt("PenaltyID"));
        penalty.setOverstayMinutes(rs.getInt("overstay_minutes"));
        penalty.setPenaltyAmount(rs.getBigDecimal("penalty_amount"));
        penalty.setIssuedAt(rs.getTimestamp("issued_at"));
        penalty.setPaymentStatus(rs.getString("payment_status"));
        penalty.setLogId(rs.getInt("log_id"));

        return penalty;
    }
}
