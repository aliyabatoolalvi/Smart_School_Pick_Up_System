package com.finallab.smartschoolpickupsystem;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "guardians")
public class GuardianEntity {
    @PrimaryKey
    @NonNull
    public String guardianId;
    public String Gname;
    public String number;
    public String CNIC;
    public String Email;
    public String QRcodeData;
    public String profile_picture_url;
}
