package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.User;

public class UserDAO {

    // Get all users
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT * FROM `USER`";
        List<User> users = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    // Find user by ID
    public User getUserById(int userID) throws SQLException {
        String sql = "SELECT * FROM `USER` WHERE UserID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }

        return null;
    }

    // Find user by email
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM `USER` WHERE Email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }

        return null;
    }

    // Insert a new user
    public int insertUser(User user) throws SQLException {
        String sql = "INSERT INTO `USER` "
                + "(FullName, Email, PasswordHash, referrer_id) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());

            if (user.getReferrerID() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, user.getReferrerID());
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    // Update user information
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE `USER` "
                + "SET FullName = ?, Email = ?, PasswordHash = ?, "
                + "referrer_id = ? WHERE UserID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());

            if (user.getReferrerID() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, user.getReferrerID());
            }

            ps.setInt(5, user.getUserID());

            return ps.executeUpdate() > 0;
        }
    }

    // Delete user by ID
    public boolean deleteUser(int userID) throws SQLException {
        String sql = "DELETE FROM `USER` WHERE UserID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userID);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into User object
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setUserID(rs.getInt("UserID"));
        user.setFullName(rs.getString("FullName"));
        user.setEmail(rs.getString("Email"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setCreatedAt(rs.getTimestamp("CreatedAt"));

        int referrerID = rs.getInt("referrer_id");

        if (rs.wasNull()) {
            user.setReferrerID(null);
        } else {
            user.setReferrerID(referrerID);
        }

        return user;
    }
}
