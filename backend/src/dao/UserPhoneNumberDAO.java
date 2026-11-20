package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.UserPhoneNumber;

public class UserPhoneNumberDAO {

    // Get all phone numbers
    public List<UserPhoneNumber> getAllPhoneNumbers() throws SQLException {
        String sql = "SELECT * FROM USER_PHONENUMBER";
        List<UserPhoneNumber> phoneNumbers = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                phoneNumbers.add(mapResultSetToPhoneNumber(rs));
            }
        }

        return phoneNumbers;
    }

    // Get phone numbers by user ID
    public List<UserPhoneNumber> getPhoneNumbersByUserId(int userID)
            throws SQLException {

        String sql = "SELECT * FROM USER_PHONENUMBER WHERE UserID = ?";
        List<UserPhoneNumber> phoneNumbers = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    phoneNumbers.add(mapResultSetToPhoneNumber(rs));
                }
            }
        }

        return phoneNumbers;
    }

    // Insert a phone number
    public boolean insertPhoneNumber(UserPhoneNumber phoneNumber)
            throws SQLException {

        String sql = "INSERT INTO USER_PHONENUMBER "
                + "(UserID, PhoneNumber) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, phoneNumber.getUserID());
            ps.setString(2, phoneNumber.getPhoneNumber());

            return ps.executeUpdate() > 0;
        }
    }

    // Update a phone number
    public boolean updatePhoneNumber(int userID, String oldPhoneNumber,
            String newPhoneNumber) throws SQLException {

        String sql = "UPDATE USER_PHONENUMBER "
                + "SET PhoneNumber = ? "
                + "WHERE UserID = ? AND PhoneNumber = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPhoneNumber);
            ps.setInt(2, userID);
            ps.setString(3, oldPhoneNumber);

            return ps.executeUpdate() > 0;
        }
    }

    // Delete a phone number
    public boolean deletePhoneNumber(int userID, String phoneNumber)
            throws SQLException {

        String sql = "DELETE FROM USER_PHONENUMBER "
                + "WHERE UserID = ? AND PhoneNumber = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userID);
            ps.setString(2, phoneNumber);

            return ps.executeUpdate() > 0;
        }
    }

    // Delete all phone numbers of a user
    public boolean deleteAllPhoneNumbers(int userID) throws SQLException {
        String sql = "DELETE FROM USER_PHONENUMBER WHERE UserID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userID);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into UserPhoneNumber object
    private UserPhoneNumber mapResultSetToPhoneNumber(ResultSet rs)
            throws SQLException {

        UserPhoneNumber phoneNumber = new UserPhoneNumber();

        phoneNumber.setUserID(rs.getInt("UserID"));
        phoneNumber.setPhoneNumber(rs.getString("PhoneNumber"));

        return phoneNumber;
    }
}
