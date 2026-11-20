package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Cancellation {

    private int cancellationID;
    private Timestamp canceledAt;
    private String reason;
    private BigDecimal refundAmount;
    private int bookingID;

    public Cancellation() {
    }

    public Cancellation(int cancellationID, Timestamp canceledAt,
                        String reason, BigDecimal refundAmount,
                        int bookingID) {
        this.cancellationID = cancellationID;
        this.canceledAt = canceledAt;
        this.reason = reason;
        this.refundAmount = refundAmount;
        this.bookingID = bookingID;
    }

    public int getCancellationID() {
        return cancellationID;
    }

    public void setCancellationID(int cancellationID) {
        this.cancellationID = cancellationID;
    }

    public Timestamp getCanceledAt() {
        return canceledAt;
    }

    public void setCanceledAt(Timestamp canceledAt) {
        this.canceledAt = canceledAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public int getBookingID() {
        return bookingID;
    }

    public void setBookingID(int bookingID) {
        this.bookingID = bookingID;
    }
}
