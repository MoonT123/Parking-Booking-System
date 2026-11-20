package model;

public class ParkingLot {

    private int lotId;
    private String lotName;
    private String address;
    private String openingHours;
    private int numberOfLevels;
    private int userID;

    public ParkingLot() {
    }

    public ParkingLot(int lotId, String lotName, String address,
                      String openingHours, int numberOfLevels,
                      int userID) {
        this.lotId = lotId;
        this.lotName = lotName;
        this.address = address;
        this.openingHours = openingHours;
        this.numberOfLevels = numberOfLevels;
        this.userID = userID;
    }

    public int getLotId() {
        return lotId;
    }

    public void setLotId(int lotId) {
        this.lotId = lotId;
    }

    public String getLotName() {
        return lotName;
    }

    public void setLotName(String lotName) {
        this.lotName = lotName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public int getNumberOfLevels() {
        return numberOfLevels;
    }

    public void setNumberOfLevels(int numberOfLevels) {
        this.numberOfLevels = numberOfLevels;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
