package com.finallab.smartschoolpickupsystem.Database

import androidx.room.*
import com.finallab.smartschoolpickupsystem.DataModels.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GuardianStudentDao {
    // Insert relationship
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuardianStudentCrossRef(crossRef: GuardianStudentCrossRef)

    // Delete relationship
    @Query("DELETE FROM GuardianStudentCrossRef WHERE studentID = :studentId")
    suspend fun deleteCrossRefsByStudentId(studentId: Int)

    // Get all students of a guardian
    @Transaction
    @Query("SELECT * FROM Guardian WHERE guardianID = :guardianId")
     fun getGuardianWithStudents(guardianId: Int): Flow<GuardianWithStudents?>

    // Get all guardians of a student
    @Transaction
    @Query("SELECT * FROM Student WHERE studentID = :studentId")
     fun getStudentWithGuardians(studentId: Int): Flow<StudentWithGuardians?>

    @Query("DELETE FROM GuardianStudentCrossRef")
    suspend fun deleteAllCrossRefs()


}

