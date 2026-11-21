package model;

public class UserPhoneNumber {

    private int userID;
    private String phoneNumber;

    public UserPhoneNumber() {
    }

    public UserPhoneNumber(int userID, String phoneNumber) {
        this.userID = userID;
        this.phoneNumber = phoneNumber;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
