package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Vehicle;

public class VehicleDAO {

    // Get all vehicles
    public List<Vehicle> getAllVehicles() throws SQLException {
        String sql = "SELECT * FROM VEHICLE";
        List<Vehicle> vehicles = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                vehicles.add(mapResultSetToVehicle(rs));
            }
        }

        return vehicles;
    }

    // Find vehicle by ID
    public Vehicle getVehicleById(int vehicleID) throws SQLException {
        String sql = "SELECT * FROM VEHICLE WHERE VehicleID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vehicleID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVehicle(rs);
                }
            }
        }

        return null;
    }

    // Get vehicles belonging to a user
    public List<Vehicle> getVehiclesByUserId(int userID) throws SQLException {
        String sql = "SELECT * FROM VEHICLE WHERE UserID = ?";
        List<Vehicle> vehicles = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehicles.add(mapResultSetToVehicle(rs));
                }
            }
        }

        return vehicles;
    }

    // Find vehicle by license plate
    public Vehicle getVehicleByLicensePlate(String licensePlate)
            throws SQLException {

        String sql = "SELECT * FROM VEHICLE WHERE LicensePlate = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, licensePlate);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVehicle(rs);
                }
            }
        }

        return null;
    }

    // Insert a new vehicle
    public int insertVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO VEHICLE "
                + "(LicensePlate, Make, Model, Color, VehicleSize, UserID) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, vehicle.getLicensePlate());
            ps.setString(2, vehicle.getMake());
            ps.setString(3, vehicle.getModel());
            ps.setString(4, vehicle.getColor());
            ps.setString(5, vehicle.getVehicleSize());
            ps.setInt(6, vehicle.getUserID());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    // Update vehicle information
    public boolean updateVehicle(Vehicle vehicle) throws SQLException {
        String sql = "UPDATE VEHICLE SET "
                + "LicensePlate = ?, Make = ?, Model = ?, "
                + "Color = ?, VehicleSize = ?, UserID = ? "
                + "WHERE VehicleID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, vehicle.getLicensePlate());
            ps.setString(2, vehicle.getMake());
            ps.setString(3, vehicle.getModel());
            ps.setString(4, vehicle.getColor());
            ps.setString(5, vehicle.getVehicleSize());
            ps.setInt(6, vehicle.getUserID());
            ps.setInt(7, vehicle.getVehicleID());

            return ps.executeUpdate() > 0;
        }
    }

    // Delete vehicle by ID
    public boolean deleteVehicle(int vehicleID) throws SQLException {
        String sql = "DELETE FROM VEHICLE WHERE VehicleID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vehicleID);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into Vehicle object
    private Vehicle mapResultSetToVehicle(ResultSet rs)
            throws SQLException {

        Vehicle vehicle = new Vehicle();

        vehicle.setVehicleID(rs.getInt("VehicleID"));
        vehicle.setLicensePlate(rs.getString("LicensePlate"));
        vehicle.setMake(rs.getString("Make"));
        vehicle.setModel(rs.getString("Model"));
        vehicle.setColor(rs.getString("Color"));
        vehicle.setVehicleSize(rs.getString("VehicleSize"));
        vehicle.setUserID(rs.getInt("UserID"));

        return vehicle;
    }
}
