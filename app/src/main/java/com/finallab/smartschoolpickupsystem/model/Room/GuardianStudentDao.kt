package com.finallab.smartschoolpickupsystem.Database

import androidx.room.*
import com.finallab.smartschoolpickupsystem.DataModels.*

@Dao
interface GuardianStudentDao {
    // Insert relationship
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuardianStudentCrossRef(crossRef: GuardianStudentCrossRef)

    // Get all students of a guardian
    @Transaction
    @Query("SELECT * FROM Guardian WHERE guardianID = :guardianId")
    suspend fun getGuardianWithStudents(guardianId: Int): GuardianWithStudents?

    // Get all guardians of a student
    @Transaction
    @Query("SELECT * FROM Student WHERE studentID = :studentId")
    suspend fun getStudentWithGuardians(studentId: Int): StudentWithGuardians?
}

