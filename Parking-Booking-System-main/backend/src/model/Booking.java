package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Booking {

    private int bookingID;
    private Timestamp startTime;
    private Timestamp endTime;
    private BigDecimal totalAmount;
    private String bookingStatus;
    private Timestamp createdAt;
    private int userID;
    private int vehicleID;
    private int lotId;
    private String bayNumber;

    public Booking() {
    }

    public Booking(int bookingID, Timestamp startTime,
                   Timestamp endTime, BigDecimal totalAmount,
                   String bookingStatus, Timestamp createdAt,
                   int userID, int vehicleID, int lotId,
                   String bayNumber) {
        this.bookingID = bookingID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalAmount = totalAmount;
        this.bookingStatus = bookingStatus;
        this.createdAt = createdAt;
        this.userID = userID;
        this.vehicleID = vehicleID;
        this.lotId = lotId;
        this.bayNumber = bayNumber;
    }

    public int getBookingID() {
        return bookingID;
    }

    public void setBookingID(int bookingID) {
        this.bookingID = bookingID;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }

    public int getLotId() {
        return lotId;
    }

    public void setLotId(int lotId) {
        this.lotId = lotId;
    }

    public String getBayNumber() {
        return bayNumber;
    }

    public void setBayNumber(String bayNumber) {
        this.bayNumber = bayNumber;
    }
}
