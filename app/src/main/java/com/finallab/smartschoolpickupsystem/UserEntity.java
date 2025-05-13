package com.finallab.smartschoolpickupsystem;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String uid;

    public String email;
    public String role;
    public String schoolId;
}
