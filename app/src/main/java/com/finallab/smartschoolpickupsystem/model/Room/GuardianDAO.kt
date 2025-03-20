package com.finallab.smartschoolpickupsystem.Room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.finallab.smartschoolpickupsystem.DataModels.Guardian

@Dao
interface GuardianDAO {
    @Insert
    fun insert(guardian: Guardian)

    @Delete
    fun delete(guardian: Guardian)

    @Query("Select * from Guardian where studentId= :studentId")
    fun getAllguardians(studentId: Int): List<Guardian>

    @Query("Select * from Guardian where id=:id")
    fun getguardianById(id: Int): Guardian
}