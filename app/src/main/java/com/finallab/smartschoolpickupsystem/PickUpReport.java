package com.finallab.smartschoolpickupsystem;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

import java.util.Date;

@IgnoreExtraProperties
@Entity(tableName = "pick_up_activities")
public class PickUpReport {

    @PrimaryKey(autoGenerate = true)
    private int reportID;

    private String studentName;
    private String studentId;
    private String guardianName;
    private String guardianId;
    private String guardName;
    private String guardId;
    private String method;
    private Date timestamp;
    private String reportText;

    public PickUpReport() {}

    public int getReportID() {
        return reportID;
    }

    public void setReportID(int reportID) {
        this.reportID = reportID;
    }

    @PropertyName("studentName")
    public String getStudentName() {
        return studentName;
    }

    @PropertyName("studentName")
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @PropertyName("studentId")
    public String getStudentId() {
        return studentId;
    }

    @PropertyName("studentId")
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    @PropertyName("guardianName")
    public String getGuardianName() {
        return guardianName;
    }

    @PropertyName("guardianName")
    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    @PropertyName("guardianId")
    public String getGuardianId() {
        return guardianId;
    }

    @PropertyName("guardianId")
    public void setGuardianId(String guardianId) {
        this.guardianId = guardianId;
    }

    @PropertyName("guardName")
    public String getGuardName() {
        return guardName;
    }

    @PropertyName("guardName")
    public void setGuardName(String guardName) {
        this.guardName = guardName;
    }

    @PropertyName("guardId")
    public String getGuardId() {
        return guardId;
    }

    @PropertyName("guardId")
    public void setGuardId(String guardId) {
        this.guardId = guardId;
    }


    @PropertyName("method")
    public String getMethod() {
        return method;
    }

    @PropertyName("method")
    public void setMethod(String method) {
        this.method = method;
    }

    @PropertyName("timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("reportText")
    public String getReportText() {
        return reportText;
    }

    @PropertyName("reportText")
    public void setReportText(String reportText) {
        this.reportText = reportText;
    }
}
