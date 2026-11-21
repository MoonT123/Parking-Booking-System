package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Payment;

public class PaymentDAO {

    // Get all payments
    public List<Payment> getAllPayments() throws SQLException {
        String sql = "SELECT * FROM PAYMENT";
        List<Payment> payments = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        }

        return payments;
    }

    // Find payment by ID
    public Payment getPaymentById(int paymentID) throws SQLException {
        String sql = "SELECT * FROM PAYMENT WHERE PaymentID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, paymentID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayment(rs);
                }
            }
        }

        return null;
    }

    // Get payments by booking ID
    public List<Payment> getPaymentsByBookingId(int bookingID)
            throws SQLException {

        String sql = "SELECT * FROM PAYMENT WHERE BookingID = ?";
        List<Payment> payments = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        }

        return payments;
    }

    // Find payment by transaction ID
    public Payment getPaymentByTransactionId(String transactionID)
            throws SQLException {

        String sql = "SELECT * FROM PAYMENT WHERE TransactionID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, transactionID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayment(rs);
                }
            }
        }

        return null;
    }

    // Insert a new payment
    public int insertPayment(Payment payment) throws SQLException {
        String sql = "INSERT INTO PAYMENT "
                + "(AmountPaid, PaymentMethod, TransactionID, "
                + "PaymentStatus, BookingID) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setBigDecimal(1, payment.getAmountPaid());
            ps.setString(2, payment.getPaymentMethod());
            ps.setString(3, payment.getTransactionID());
            ps.setString(4, payment.getPaymentStatus());
            ps.setInt(5, payment.getBookingID());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    // Update payment information
    public boolean updatePayment(Payment payment) throws SQLException {
        String sql = "UPDATE PAYMENT SET "
                + "AmountPaid = ?, PaymentMethod = ?, TransactionID = ?, "
                + "PaymentStatus = ?, BookingID = ? "
                + "WHERE PaymentID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, payment.getAmountPaid());
            ps.setString(2, payment.getPaymentMethod());
            ps.setString(3, payment.getTransactionID());
            ps.setString(4, payment.getPaymentStatus());
            ps.setInt(5, payment.getBookingID());
            ps.setInt(6, payment.getPaymentID());

            return ps.executeUpdate() > 0;
        }
    }

    // Update payment status
    public boolean updatePaymentStatus(int paymentID, String status)
            throws SQLException {

        String sql = "UPDATE PAYMENT SET PaymentStatus = ? "
                + "WHERE PaymentID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, paymentID);

            return ps.executeUpdate() > 0;
        }
    }

    // Delete payment
    public boolean deletePayment(int paymentID) throws SQLException {
        String sql = "DELETE FROM PAYMENT WHERE PaymentID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, paymentID);

            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into Payment object
    private Payment mapResultSetToPayment(ResultSet rs)
            throws SQLException {

        Payment payment = new Payment();

        payment.setPaymentID(rs.getInt("PaymentID"));
        payment.setAmountPaid(rs.getBigDecimal("AmountPaid"));
        payment.setPaymentMethod(rs.getString("PaymentMethod"));
        payment.setTransactionID(rs.getString("TransactionID"));
        payment.setPaymentStatus(rs.getString("PaymentStatus"));
        payment.setPaidAt(rs.getTimestamp("paid_at"));
        payment.setBookingID(rs.getInt("BookingID"));

        return payment;
    }
}
