package com.finallab.smartschoolpickupsystem;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PickUpReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReports(List<PickUpReport> reports);

    @Query("SELECT * FROM pick_up_activities ORDER BY timestamp DESC")
    List<PickUpReport> getAllReports();
}
