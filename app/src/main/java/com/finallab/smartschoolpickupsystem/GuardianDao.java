package com.finallab.smartschoolpickupsystem;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface GuardianDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGuardian(GuardianEntity guardian);

    @Query("SELECT * FROM guardians WHERE guardianId = :uid LIMIT 1")
    GuardianEntity getGuardianById(String uid);
}
