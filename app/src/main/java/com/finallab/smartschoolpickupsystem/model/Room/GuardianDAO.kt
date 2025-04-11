    package com.finallab.smartschoolpickupsystem.Room

    import androidx.room.Dao
    import androidx.room.Delete
    import androidx.room.Insert
    import androidx.room.OnConflictStrategy
    import androidx.room.Query
    import androidx.room.Transaction
    import androidx.room.Update
    import com.finallab.smartschoolpickupsystem.DataModels.StudentWithGuardians
    import com.finallab.smartschoolpickupsystem.DataModels.GuardianWithStudents
    import com.finallab.smartschoolpickupsystem.DataModels.Guardian
    import kotlinx.coroutines.flow.Flow

    @Dao
    interface GuardianDAO {
        @Query("SELECT COUNT(*) FROM Guardian")
        fun getGuardianCount(): Int
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun upsertGuardian(guardian: Guardian)
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertGuardian(guardian: Guardian): Long

        @Delete
        suspend fun deleteGuardian(guardian: Guardian)
        @Query("SELECT * FROM Guardian WHERE userId = :userId")
        suspend fun getGuardiansByUserId(userId: String): List<Guardian>

        @Query("SELECT * FROM Guardian WHERE CNIC = :CNIC")
        suspend fun getGuardianByCNIC(CNIC: String): Guardian?

        @Query("SELECT * FROM Guardian WHERE guardianID = :guardianID")
        suspend fun getGuardianById(guardianID: Int): Guardian?

        // Fetch all guardians (for general use)
        @Query("SELECT * FROM Guardian")
        fun getAllGuardians(): Flow<List<Guardian>>

        // Fetch guardians by studentId (specific use)
//        @Query("SELECT * FROM Guardian WHERE studentId = :studentID")
//        fun getGuardiansByStudentId(studentID: Int): Flow<List<Guardian>>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertAllGuardians(guardians: List<Guardian>)

        @Update
        fun updateGuardian(guardian: Guardian)

        @Transaction
        @Query("SELECT * FROM Student WHERE studentID = :studentId")
        fun getStudentWithGuardians(studentId: Int): Flow<StudentWithGuardians>

    }