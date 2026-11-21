package model;

public class Vehicle {

    private int vehicleID;
    private String licensePlate;
    private String make;
    private String model;
    private String color;
    private String vehicleSize;
    private int userID;

    public Vehicle() {
    }

    public Vehicle(int vehicleID, String licensePlate,
                   String make, String model, String color,
                   String vehicleSize, int userID) {
        this.vehicleID = vehicleID;
        this.licensePlate = licensePlate;
        this.make = make;
        this.model = model;
        this.color = color;
        this.vehicleSize = vehicleSize;
        this.userID = userID;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getVehicleSize() {
        return vehicleSize;
    }

    public void setVehicleSize(String vehicleSize) {
        this.vehicleSize = vehicleSize;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
