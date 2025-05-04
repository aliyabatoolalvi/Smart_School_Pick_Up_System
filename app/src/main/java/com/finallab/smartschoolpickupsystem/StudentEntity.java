package com.finallab.smartschoolpickupsystem;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "students")
public class StudentEntity {
    @PrimaryKey
    @NonNull
    public String studentId;
    public String Sname;
    public String reg;
    public String studentClass;
    public String section;
    public String image;
}
