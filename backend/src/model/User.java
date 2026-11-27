package model;

import java.sql.Timestamp;

public class User {

    private int userID;
    private String fullName;
    private String email;
    private String passwordHash;
    private Timestamp createdAt;
    private Integer referrerID;

    public User() {
    }

    public User(int userID, String fullName, String email,
                String passwordHash, Timestamp createdAt,
                Integer referrerID) {
        this.userID = userID;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.referrerID = referrerID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getReferrerID() {
        return referrerID;
    }

    public void setReferrerID(Integer referrerID) {
        this.referrerID = referrerID;
    }
}
