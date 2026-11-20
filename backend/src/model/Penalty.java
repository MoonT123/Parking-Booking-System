package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Penalty {

    private int penaltyID;
    private int overstayMinutes;
    private BigDecimal penaltyAmount;
    private Timestamp issuedAt;
    private String paymentStatus;
    private int logId;

    public Penalty() {
    }

    public Penalty(int penaltyID, int overstayMinutes,
                   BigDecimal penaltyAmount, Timestamp issuedAt,
                   String paymentStatus, int logId) {
        this.penaltyID = penaltyID;
        this.overstayMinutes = overstayMinutes;
        this.penaltyAmount = penaltyAmount;
        this.issuedAt = issuedAt;
        this.paymentStatus = paymentStatus;
        this.logId = logId;
    }

    public int getPenaltyID() {
        return penaltyID;
    }

    public void setPenaltyID(int penaltyID) {
        this.penaltyID = penaltyID;
    }

    public int getOverstayMinutes() {
        return overstayMinutes;
    }

    public void setOverstayMinutes(int overstayMinutes) {
        this.overstayMinutes = overstayMinutes;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public Timestamp getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Timestamp issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }
}
