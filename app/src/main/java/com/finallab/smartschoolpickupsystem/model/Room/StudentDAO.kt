package com.finallab.smartschoolpickupsystem.Room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.finallab.smartschoolpickupsystem.DataModels.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDAO {

    // Insert or update a single student
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudent(student: Student)

    // Insert a new student and return generated Room ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    // Insert a list of students (bulk insert)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStudents(student: List<Student>)

    // Update existing student
    @Update
    fun updateStudent(student: Student)

    // Delete a student
    @Delete
    suspend fun deleteStudent(student: Student)

    // Delete all students
    @Query("DELETE FROM Student")
    suspend fun deleteAllStudents()

    // Fetch a single student by primary key (Room ID)
    @Query("SELECT * FROM Student WHERE studentID = :studentId")
    suspend fun getStudentById(studentId: Int): Student?

    // Fetch a student by Firestore document ID
    @Query("SELECT * FROM Student WHERE studentDocId = :studentDocId LIMIT 1")
    suspend fun getStudentByDocId(studentDocId: String): Student?

    // Fetch all students (reactive Flow)
    @Query("SELECT * FROM Student")
    fun getAllStudents(): Flow<List<Student>>

    // Fetch all students belonging to a specific school (userId)
    @Query("SELECT * FROM Student WHERE userId = :userId")
    suspend fun getStudentsByUserId(userId: String): List<Student>

    // Count total students
    @Query("SELECT COUNT(*) FROM Student")
    fun getStudentCount(): Int
}
