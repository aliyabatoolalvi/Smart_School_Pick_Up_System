package com.finallab.smartschoolpickupsystem;

public class User {
    private String schoolName;
    private String schoolAddress;
    private String email;

    public User() {
        // Default constructor required for Firestore
    }

    public User(String schoolName, String schoolAddress, String email) {
        this.schoolName = schoolName;
        this.schoolAddress = schoolAddress;
        this.email = email;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolAddress() {
        return schoolAddress;
    }

    public void setSchoolAddress(String schoolAddress) {
        this.schoolAddress = schoolAddress;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

