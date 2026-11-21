package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Payment {

    private int paymentID;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private String transactionID;
    private String paymentStatus;
    private Timestamp paidAt;
    private int bookingID;

    public Payment() {
    }

    public Payment(int paymentID, BigDecimal amountPaid,
                   String paymentMethod, String transactionID,
                   String paymentStatus, Timestamp paidAt,
                   int bookingID) {
        this.paymentID = paymentID;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.transactionID = transactionID;
        this.paymentStatus = paymentStatus;
        this.paidAt = paidAt;
        this.bookingID = bookingID;
    }

    public int getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(int paymentID) {
        this.paymentID = paymentID;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Timestamp getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }

    public int getBookingID() {
        return bookingID;
    }

    public void setBookingID(int bookingID) {
        this.bookingID = bookingID;
    }
}
