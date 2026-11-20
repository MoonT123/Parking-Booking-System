package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.ParkingLot;

public class ParkingLotDAO {

    // Get all parking lots
    public List<ParkingLot> getAllParkingLots() throws SQLException {
        String sql = "SELECT * FROM PARKINGLOT";
        List<ParkingLot> lots = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lots.add(mapResultSetToParkingLot(rs));
            }
        }

        return lots;
    }

    // Find parking lot by ID
    public ParkingLot getParkingLotById(int lotId) throws SQLException {
        String sql = "SELECT * FROM PARKINGLOT WHERE lot_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToParkingLot(rs);
                }
            }
        }

        return null;
    }

    // Search parking lots by name
    public List<ParkingLot> searchParkingLotsByName(String name)
            throws SQLException {

        String sql = "SELECT * FROM PARKINGLOT WHERE LotName LIKE ?";
        List<ParkingLot> lots = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lots.add(mapResultSetToParkingLot(rs));
                }
            }
        }

        return lots;
    }

    // Get parking lots managed by a user
    public List<ParkingLot> getParkingLotsByUserId(int userID)
            throws SQLException {

        String sql = "SELECT * FROM PARKINGLOT WHERE UserID = ?";
        List<ParkingLot> lots = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lots.add(mapResultSetToParkingLot(rs));
                }
            }
        }

        return lots;
    }

    // Insert a new parking lot
    public int insertParkingLot(ParkingLot lot) throws SQLException {
        String sql = "INSERT INTO PARKINGLOT "
                + "(LotName, Address, opening_hours, number_of_levels, UserID) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, lot.getLotName());
            ps.setString(2, lot.getAddress());
            ps.setString(3, lot.getOpeningHours());
            ps.setInt(4, lot.getNumberOfLevels());
            ps.setInt(5, lot.getUserID());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    // Update parking lot information
    public boolean updateParkingLot(ParkingLot lot) throws SQLException {
        String sql = "UPDATE PARKINGLOT SET "
                + "LotName = ?, Address = ?, opening_hours = ?, "
                + "number_of_levels = ?, UserID = ? "
                + "WHERE lot_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lot.getLotName());
            ps.setString(2, lot.getAddress());
            ps.setString(3, lot.getOpeningHours());
            ps.setInt(4, lot.getNumberOfLevels());
            ps.setInt(5, lot.getUserID());
            ps.setInt(6, lot.getLotId());

            return ps.executeUpdate() > 0;
        }
    }

    // Delete parking lot by ID
    public boolean deleteParkingLot(int lotId) throws SQLException {
        String sql = "DELETE FROM PARKINGLOT WHERE lot_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into ParkingLot object
    private ParkingLot mapResultSetToParkingLot(ResultSet rs)
            throws SQLException {

        ParkingLot lot = new ParkingLot();

        lot.setLotId(rs.getInt("lot_id"));
        lot.setLotName(rs.getString("LotName"));
        lot.setAddress(rs.getString("Address"));
        lot.setOpeningHours(rs.getString("opening_hours"));
        lot.setNumberOfLevels(rs.getInt("number_of_levels"));
        lot.setUserID(rs.getInt("UserID"));

        return lot;
    }
}
