package com.finallab.smartschoolpickupsystem;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class PickUpReport {

    private String studentName;
    private Timestamp timestamp;
    private String deviation;
    private String pickedBy;
    private String pickedByUID;
    private String method;
    private String reportText;
    private String guardianId;     // 🔄 added
    private String guardName;      // 🔄 added
    private String studentId;      // 🔄 added
    private String schoolId;       // 🔄 added

    public PickUpReport() {}

    public PickUpReport(String studentName, Timestamp timestamp, String deviation, String pickedBy, String pickedByUID) {
        this.studentName = studentName;
        this.timestamp = timestamp;
        this.deviation = deviation;
        this.pickedBy = pickedBy;
        this.pickedByUID = pickedByUID;
    }

    // --- Getters & Setters ---

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviation() {
        return deviation;
    }

    public void setDeviation(String deviation) {
        this.deviation = deviation;
    }

    public String getPickedBy() {
        return pickedBy;
    }

    public void setPickedBy(String pickedBy) {
        this.pickedBy = pickedBy;
    }

    public String getPickedByUID() {
        return pickedByUID;
    }

    public void setPickedByUID(String pickedByUID) {
        this.pickedByUID = pickedByUID;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getReportText() {
        return reportText;
    }

    public void setReportText(String reportText) {
        this.reportText = reportText;
    }

    public String getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(String guardianId) {
        this.guardianId = guardianId;
    }

    public String getGuardName() {
        return guardName;
    }

    public void setGuardName(String guardName) {
        this.guardName = guardName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }
}
