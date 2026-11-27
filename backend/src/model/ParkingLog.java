package model;

import java.sql.Timestamp;

public class ParkingLog {

    private int logId;
    private Integer bookingID;

    public Integer getBookingID() {
        return bookingID;
    }

    public void setBookingID(Integer bookingID) {
        this.bookingID = bookingID;
    }
    private Timestamp checkInTime;
    private Timestamp checkOutTime;
    private int lotId;
    private Integer userID;
    private int vehicleID;

    public ParkingLog() {
    }

    public ParkingLog(int logId, Timestamp checkInTime,
                      Timestamp checkOutTime, int lotId,
                      Integer userID, int vehicleID) {
        this.logId = logId;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.lotId = lotId;
        this.userID = userID;
        this.vehicleID = vehicleID;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public Timestamp getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Timestamp checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Timestamp getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Timestamp checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public int getLotId() {
        return lotId;
    }

    public void setLotId(int lotId) {
        this.lotId = lotId;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }
}
