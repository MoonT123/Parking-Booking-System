package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

    // Availability for a requested window, ordered by fit and distance.
    public List<ParkingBay> getAvailableBays(int lotId, int vehicleId, Timestamp start, Timestamp end)
            throws SQLException {
        if (start == null || end == null || !end.after(start)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        String sql = """
                SELECT pb.* FROM PARKINGBAY pb
                JOIN VEHICLE_BAY_SIZE fit ON fit.bay_size = pb.bay_size
                JOIN VEHICLE v ON v.VehicleSize = fit.vehicle_size AND v.VehicleID = ?
                WHERE pb.lot_id = ? AND pb.bay_status <> 'MAINTENANCE'
                  AND NOT EXISTS (
                    SELECT 1 FROM BOOKING b
                    WHERE b.lot_id = pb.lot_id AND b.bay_number = pb.bay_number
                      AND b.BookingStatus IN ('PENDING', 'CONFIRMED')
                      AND b.StartTime < ? AND b.EndTime > ?
                  )
                ORDER BY fit.fit_rank, pb.distance_to_exit_m IS NULL, pb.distance_to_exit_m, pb.bay_number
                """;
        List<ParkingBay> bays = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ps.setInt(2, lotId);
            ps.setTimestamp(3, end);
            ps.setTimestamp(4, start);
            try (ResultSet rows = ps.executeQuery()) {
                while (rows.next()) {
                    bays.add(mapResultSetToParkingBay(rows));
                }
            }
        }
        return bays;
    }

    // Insert parking bay
    public boolean insertParkingBay(ParkingBay bay) throws SQLException {
        String sql = "INSERT INTO PARKINGBAY "
                + "(lot_id, bay_number, bay_status, bay_size, bay_type, "
                + "level_no, zone_code, distance_to_exit_m, map_node_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bay.getLotId());
            ps.setString(2, bay.getBayNumber());
            ps.setString(3, bay.getBayStatus());
            ps.setString(4, bay.getBaySize());
            ps.setString(5, bay.getBayType());
            ps.setObject(6, bay.getLevelNo(), java.sql.Types.INTEGER);
            ps.setString(7, bay.getZoneCode());
            ps.setBigDecimal(8, bay.getDistanceToExitM());
            ps.setString(9, bay.getMapNodeId());

            return ps.executeUpdate() > 0;
        }
    }

    // Update parking bay
    public boolean updateParkingBay(ParkingBay bay) throws SQLException {
        String sql = "UPDATE PARKINGBAY SET "
                + "bay_status = ?, bay_size = ?, bay_type = ?, "
                + "level_no = ?, zone_code = ?, distance_to_exit_m = ?, map_node_id = ? "
                + "WHERE lot_id = ? AND bay_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bay.getBayStatus());
            ps.setString(2, bay.getBaySize());
            ps.setString(3, bay.getBayType());
            ps.setObject(4, bay.getLevelNo(), java.sql.Types.INTEGER);
            ps.setString(5, bay.getZoneCode());
            ps.setBigDecimal(6, bay.getDistanceToExitM());
            ps.setString(7, bay.getMapNodeId());
            ps.setInt(8, bay.getLotId());
            ps.setString(9, bay.getBayNumber());

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
        bay.setLevelNo(rs.getObject("level_no", Integer.class));
        bay.setZoneCode(rs.getString("zone_code"));
        bay.setDistanceToExitM(rs.getBigDecimal("distance_to_exit_m"));
        bay.setMapNodeId(rs.getString("map_node_id"));

        return bay;
    }
}
