package com.finallab.smartschoolpickupsystem;

import java.util.List;

public class Guardian {

    private String guardianId;
    private String guardianDocId;
    private String Gname;
    private String number;
    private String CNIC;
    private String Email;
    private String QRcodeData;
    private List<String> students;


    private String profile_picture_url;


    public Guardian() {}
    public List<String> getStudents() {
        return students;
    }

    public void setStudents(List<String> students) {
        this.students = students;
    }

    // Getters and Setters
    public String getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(String guardianId) {
        this.guardianId = guardianId;
    }

    public String getGname() {
        return Gname;
    }

    public void setGname(String Gname) {
        this.Gname = Gname;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCNIC() {
        return CNIC;
    }

    public void setCNIC(String CNIC) {
        this.CNIC = CNIC;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getQRcodeData() {
        return QRcodeData;
    }

    public void setQRcodeData(String QRcodeData) {
        this.QRcodeData = QRcodeData;
    }
    public String getProfile_picture_url() {
        return profile_picture_url;
    }

    public void setProfile_picture_url(String profile_picture_url) {
        this.profile_picture_url = profile_picture_url;
    }
    public String getGuardianDocId() {
        return guardianDocId;
    }

    public void setGuardianDocId(String guardianDocId) {
        this.guardianDocId = guardianDocId;
    }



}
