package com.finallab.smartschoolpickupsystem;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.finallab.smartschoolpickupsystem.Room.Converters;
import com.google.firebase.Timestamp;

@Entity(tableName = "pick_up_activities")
public class GuardianReportEntity {
    @PrimaryKey @NonNull
    public String reportId;

    public String CNIC;
    public String guardianId;
    public String guardianName;
    public String guardianUID;
    public String method;
    @TypeConverters(Converters.class) // Add this line to use timestamp converter
    public Timestamp timestamp;
    public String pickUpTime;  // Stored as formatted String
    public String reportText;
    public String studentId;
    public String studentName;
}
