package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.ParkingBay;

public class ParkingBayDAO {

    // Get all parking bays
    public List<ParkingBay> getAllParkingBays() throws SQLException {
        String sql = "SELECT * FROM PARKINGBAY";
        List<ParkingBay> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToParkingBay(rs));
            }
        }

        return list;
    }

    // Get parking bay by composite primary key
    public ParkingBay getParkingBay(int lotId, String bayNumber)
            throws SQLException {

        String sql = "SELECT * FROM PARKINGBAY "
                + "WHERE lot_id = ? AND bay_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);
            ps.setString(2, bayNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToParkingBay(rs);
                }
            }
        }

        return null;
    }

    // Get all bays in a parking lot
    public List<ParkingBay> getBaysByLotId(int lotId) throws SQLException {
        String sql = "SELECT * FROM PARKINGBAY WHERE lot_id = ?";
        List<ParkingBay> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToParkingBay(rs));
                }
            }
        }

        return list;
    }

    // Get available bays in a parking lot
    public List<ParkingBay> getAvailableBays(int lotId) throws SQLException {
        String sql = "SELECT * FROM PARKINGBAY "
                + "WHERE lot_id = ? AND bay_status = 'AVAILABLE'";

        List<ParkingBay> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToParkingBay(rs));
                }
            }
        }

        return list;
    }

    // Insert parking bay
    public boolean insertParkingBay(ParkingBay bay) throws SQLException {
        String sql = "INSERT INTO PARKINGBAY "
                + "(lot_id, bay_number, bay_status, bay_size, bay_type) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bay.getLotId());
            ps.setString(2, bay.getBayNumber());
            ps.setString(3, bay.getBayStatus());
            ps.setString(4, bay.getBaySize());
            ps.setString(5, bay.getBayType());

            return ps.executeUpdate() > 0;
        }
    }

    // Update parking bay
    public boolean updateParkingBay(ParkingBay bay) throws SQLException {
        String sql = "UPDATE PARKINGBAY SET "
                + "bay_status = ?, bay_size = ?, bay_type = ? "
                + "WHERE lot_id = ? AND bay_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bay.getBayStatus());
            ps.setString(2, bay.getBaySize());
            ps.setString(3, bay.getBayType());
            ps.setInt(4, bay.getLotId());
            ps.setString(5, bay.getBayNumber());

            return ps.executeUpdate() > 0;
        }
    }

    // Update bay status
    public boolean updateBayStatus(int lotId, String bayNumber,
                                   String status) throws SQLException {

        String sql = "UPDATE PARKINGBAY SET bay_status = ? "
                + "WHERE lot_id = ? AND bay_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, lotId);
            ps.setString(3, bayNumber);

            return ps.executeUpdate() > 0;
        }
    }

    // Delete parking bay
    public boolean deleteParkingBay(int lotId, String bayNumber)
            throws SQLException {

        String sql = "DELETE FROM PARKINGBAY "
                + "WHERE lot_id = ? AND bay_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);
            ps.setString(2, bayNumber);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into ParkingBay object
    private ParkingBay mapResultSetToParkingBay(ResultSet rs)
            throws SQLException {

        ParkingBay bay = new ParkingBay();

        bay.setLotId(rs.getInt("lot_id"));
        bay.setBayNumber(rs.getString("bay_number"));
        bay.setBayStatus(rs.getString("bay_status"));
        bay.setBaySize(rs.getString("bay_size"));
        bay.setBayType(rs.getString("bay_type"));

        return bay;
    }
}
