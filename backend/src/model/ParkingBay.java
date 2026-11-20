package model;

public class ParkingBay {

    private int lotId;
    private String bayNumber;
    private String bayStatus;
    private String baySize;
    private String bayType;

    public ParkingBay() {
    }

    public ParkingBay(int lotId, String bayNumber,
                      String bayStatus, String baySize,
                      String bayType) {
        this.lotId = lotId;
        this.bayNumber = bayNumber;
        this.bayStatus = bayStatus;
        this.baySize = baySize;
        this.bayType = bayType;
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

    public String getBayStatus() {
        return bayStatus;
    }

    public void setBayStatus(String bayStatus) {
        this.bayStatus = bayStatus;
    }

    public String getBaySize() {
        return baySize;
    }

    public void setBaySize(String baySize) {
        this.baySize = baySize;
    }

    public String getBayType() {
        return bayType;
    }

    public void setBayType(String bayType) {
        this.bayType = bayType;
    }
}
