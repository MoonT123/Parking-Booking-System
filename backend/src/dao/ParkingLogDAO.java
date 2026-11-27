package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.ParkingLog;

public class ParkingLogDAO {

    // Get all parking logs
    public List<ParkingLog> getAllParkingLogs() throws SQLException {
        String sql = "SELECT * FROM PARKINGLOGS";
        List<ParkingLog> logs = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                logs.add(mapResultSetToParkingLog(rs));
            }
        }

        return logs;
    }

    // Find parking log by ID
    public ParkingLog getParkingLogById(int logId) throws SQLException {
        String sql = "SELECT * FROM PARKINGLOGS WHERE log_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, logId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToParkingLog(rs);
                }
            }
        }

        return null;
    }

    // Get logs by user ID
    public List<ParkingLog> getParkingLogsByUserId(int userID)
            throws SQLException {

        String sql = "SELECT * FROM PARKINGLOGS WHERE UserID = ?";
        List<ParkingLog> logs = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToParkingLog(rs));
                }
            }
        }

        return logs;
    }

    // Insert a new parking log
    public int insertParkingLog(ParkingLog log) throws SQLException {
        String sql = "INSERT INTO PARKINGLOGS "
                + "(check_in_time, check_out_time, lot_id, UserID, VehicleID) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, log.getCheckInTime());
            ps.setTimestamp(2, log.getCheckOutTime());
            ps.setInt(3, log.getLotId());

            if (log.getUserID() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, log.getUserID());
            }

            ps.setInt(5, log.getVehicleID());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    // Update parking log
    public boolean updateParkingLog(ParkingLog log) throws SQLException {
        String sql = "UPDATE PARKINGLOGS SET "
                + "check_in_time = ?, check_out_time = ?, "
                + "lot_id = ?, UserID = ?, VehicleID = ? "
                + "WHERE log_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, log.getCheckInTime());
            ps.setTimestamp(2, log.getCheckOutTime());
            ps.setInt(3, log.getLotId());

            if (log.getUserID() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, log.getUserID());
            }

            ps.setInt(5, log.getVehicleID());
            ps.setInt(6, log.getLogId());

            return ps.executeUpdate() > 0;
        }
    }

    // Update check-out time
    public boolean updateCheckOutTime(int logId,
            java.sql.Timestamp checkOutTime) throws SQLException {

        String sql = "UPDATE PARKINGLOGS SET check_out_time = ? "
                + "WHERE log_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, checkOutTime);
            ps.setInt(2, logId);

            return ps.executeUpdate() > 0;
        }
    }

    // Delete parking log
    public boolean deleteParkingLog(int logId) throws SQLException {
        String sql = "DELETE FROM PARKINGLOGS WHERE log_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, logId);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into ParkingLog object
    private ParkingLog mapResultSetToParkingLog(ResultSet rs)
            throws SQLException {

        ParkingLog log = new ParkingLog();

        log.setLogId(rs.getInt("log_id"));
        log.setCheckInTime(rs.getTimestamp("check_in_time"));
        log.setCheckOutTime(rs.getTimestamp("check_out_time"));
        log.setLotId(rs.getInt("lot_id"));

        int userID = rs.getInt("UserID");
        if (rs.wasNull()) {
            log.setUserID(null);
        } else {
            log.setUserID(userID);
        }

        log.setVehicleID(rs.getInt("VehicleID"));

        return log;
    }
}
