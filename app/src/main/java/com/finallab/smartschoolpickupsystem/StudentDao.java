package com.finallab.smartschoolpickupsystem;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStudents(List<StudentEntity> students);

    @Query("SELECT * FROM students")
    List<StudentEntity> getAllStudents();
}
