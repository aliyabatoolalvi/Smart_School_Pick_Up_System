package com.finallab.smartschoolpickupsystem;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
@Dao
public interface GuardianReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReports(List<GuardianReportEntity> reports);

        @Query("SELECT * FROM pick_up_activities WHERE guardianUID = :guardianUID ORDER BY pickUpTime DESC")
        List<GuardianReportEntity> getReportsForGuardian(String guardianUID);
    }


