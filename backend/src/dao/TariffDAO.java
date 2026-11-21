package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Tariff;

public class TariffDAO {

    // Get all tariffs
    public List<Tariff> getAllTariffs() throws SQLException {
        String sql = "SELECT * FROM TARIFF";
        List<Tariff> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToTariff(rs));
            }
        }

        return list;
    }

    // Get tariff by ID
    public Tariff getTariffById(int tariffID) throws SQLException {
        String sql = "SELECT * FROM TARIFF WHERE Tariff_ID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tariffID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTariff(rs);
                }
            }
        }

        return null;
    }

    // Get tariffs by day of week
    public List<Tariff> getTariffsByDay(String dayOfWeek)
            throws SQLException {

        String sql = "SELECT * FROM TARIFF WHERE day_of_week = ?";
        List<Tariff> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dayOfWeek);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTariff(rs));
                }
            }
        }

        return list;
    }

    // Insert tariff
    public boolean insertTariff(Tariff tariff) throws SQLException {
        String sql = "INSERT INTO TARIFF "
                + "(day_of_week, start_hour, end_hour, hourly_rate) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tariff.getDayOfWeek());
            ps.setTime(2, tariff.getStartHour());
            ps.setTime(3, tariff.getEndHour());
            ps.setBigDecimal(4, tariff.getHourlyRate());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        tariff.setTariffID(keys.getInt(1));
                    }
                }
                return true;
            }
        }

        return false;
    }

    // Update tariff
    public boolean updateTariff(Tariff tariff) throws SQLException {
        String sql = "UPDATE TARIFF SET "
                + "day_of_week = ?, start_hour = ?, "
                + "end_hour = ?, hourly_rate = ? "
                + "WHERE Tariff_ID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tariff.getDayOfWeek());
            ps.setTime(2, tariff.getStartHour());
            ps.setTime(3, tariff.getEndHour());
            ps.setBigDecimal(4, tariff.getHourlyRate());
            ps.setInt(5, tariff.getTariffID());

            return ps.executeUpdate() > 0;
        }
    }

    // Delete tariff
    public boolean deleteTariff(int tariffID) throws SQLException {
        String sql = "DELETE FROM TARIFF WHERE Tariff_ID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tariffID);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into Tariff object
    private Tariff mapResultSetToTariff(ResultSet rs)
            throws SQLException {

        Tariff tariff = new Tariff();

        tariff.setTariffID(rs.getInt("Tariff_ID"));
        tariff.setDayOfWeek(rs.getString("day_of_week"));
        tariff.setStartHour(rs.getTime("start_hour"));
        tariff.setEndHour(rs.getTime("end_hour"));
        tariff.setHourlyRate(rs.getBigDecimal("hourly_rate"));

        return tariff;
    }
}
