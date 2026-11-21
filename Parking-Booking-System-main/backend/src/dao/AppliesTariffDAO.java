package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.AppliesTariff;

public class AppliesTariffDAO {

    // Get all applied tariffs
    public List<AppliesTariff> getAllAppliesTariffs() throws SQLException {
        String sql = "SELECT * FROM APPLIESTARIF";
        List<AppliesTariff> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToAppliesTariff(rs));
            }
        }

        return list;
    }

    // Get applied tariffs by parking lot ID
    public List<AppliesTariff> getByLotId(int lotId) throws SQLException {
        String sql = "SELECT * FROM APPLIESTARIF WHERE lot_id = ?";
        List<AppliesTariff> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppliesTariff(rs));
                }
            }
        }

        return list;
    }

    // Get one applied tariff by composite primary key
    public AppliesTariff getById(int lotId, int tariffId)
            throws SQLException {

        String sql = "SELECT * FROM APPLIESTARIF "
                + "WHERE lot_id = ? AND Tariff_ID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);
            ps.setInt(2, tariffId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAppliesTariff(rs);
                }
            }
        }

        return null;
    }

    // Insert an applied tariff
    public boolean insertAppliesTariff(AppliesTariff appliesTariff)
            throws SQLException {

        String sql = "INSERT INTO APPLIESTARIF "
                + "(lot_id, Tariff_ID, effective_date) "
                + "VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appliesTariff.getLotId());
            ps.setInt(2, appliesTariff.getTariffID());
            ps.setDate(3, appliesTariff.getEffectiveDate());

            return ps.executeUpdate() > 0;
        }
    }

    // Update effective date
    public boolean updateAppliesTariff(AppliesTariff appliesTariff)
            throws SQLException {

        String sql = "UPDATE APPLIESTARIF "
                + "SET effective_date = ? "
                + "WHERE lot_id = ? AND Tariff_ID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, appliesTariff.getEffectiveDate());
            ps.setInt(2, appliesTariff.getLotId());
            ps.setInt(3, appliesTariff.getTariffID());

            return ps.executeUpdate() > 0;
        }
    }

    // Delete an applied tariff
    public boolean deleteAppliesTariff(int lotId, int tariffId)
            throws SQLException {

        String sql = "DELETE FROM APPLIESTARIF "
                + "WHERE lot_id = ? AND Tariff_ID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lotId);
            ps.setInt(2, tariffId);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into AppliesTariff object
    private AppliesTariff mapResultSetToAppliesTariff(ResultSet rs)
            throws SQLException {

        AppliesTariff appliesTariff = new AppliesTariff();

        appliesTariff.setLotId(rs.getInt("lot_id"));
        appliesTariff.setTariffID(rs.getInt("Tariff_ID"));
        appliesTariff.setEffectiveDate(rs.getDate("effective_date"));

        return appliesTariff;
    }
}
