package com.finallab.smartschoolpickupsystem.Room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.DataModels.StudentWithGuardians
import kotlinx.coroutines.flow.Flow

@Dao
interface GuardianDAO {

    // Insert or update a single guardian
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGuardian(guardian: Guardian)

    // Insert a new guardian and return generated Room ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuardian(guardian: Guardian): Long

    // Insert a list of guardians (bulk insert)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGuardians(guardians: List<Guardian>)

    // Update an existing guardian
    @Update
    fun updateGuardian(guardian: Guardian)

    // Delete a guardian
    @Delete
    suspend fun deleteGuardian(guardian: Guardian)
    @Query("DELETE FROM Guardian")
    suspend fun deleteAllGuardians()
    // Fetch a single guardian by primary key (Room ID)
    @Query("SELECT * FROM Guardian WHERE guardianID = :guardianID")
    suspend fun getGuardianById(guardianID: Int): Guardian?

    // Fetch a guardian by Firestore document ID
    @Query("SELECT * FROM Guardian WHERE guardianDocId = :guardianDocId LIMIT 1")
    suspend fun getGuardianByDocId(guardianDocId: String): Guardian?

    // Fetch a guardian by CNIC
    @Query("SELECT * FROM Guardian WHERE CNIC = :CNIC")
    suspend fun getGuardianByCNIC(CNIC: String): Guardian?

    // Fetch all guardians belonging to a specific school (userId)
    @Query("SELECT * FROM Guardian WHERE userId = :userId")
    suspend fun getGuardiansByUserId(userId: String): List<Guardian>

    // Fetch all guardians (reactive Flow)
    @Query("SELECT * FROM Guardian")
    fun getAllGuardians(): Flow<List<Guardian>>

    // Count total guardians
    @Query("SELECT COUNT(*) FROM Guardian")
    fun getGuardianCount(): Int

    // Fetch all guardians related to a specific student
    @Transaction
    @Query("SELECT * FROM Student WHERE studentID = :studentId")
    fun getStudentWithGuardians(studentId: Int): Flow<StudentWithGuardians>
}
