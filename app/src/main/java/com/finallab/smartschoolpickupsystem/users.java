package com.finallab.smartschoolpickupsystem;

public class users {
    private String email;
    private String role;
    private String schoolId;
    private String uid;

    public users(String email, String role, String schoolId, String uid) {
        this.email = email;
        this.role = role;
        this.schoolId = schoolId;
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
