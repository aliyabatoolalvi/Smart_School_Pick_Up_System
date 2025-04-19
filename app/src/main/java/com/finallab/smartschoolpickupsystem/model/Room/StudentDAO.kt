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
//    @Insert
//    fun insert(student: Student)
//
//    @Delete
//    fun delete(student: Student)
//
//    @Query("Select * from Student")
//    fun getAllStudents(): List<Student>
//
//    @Query("SELECT * FROM Student WHERE userId = :userId")
//        fun getStudentsByUserId(userId: String): List<Student>
//
//
//
//
//    @Query("Select * from Student where id=:id")
//    fun getstudentById(id: Int): Student

    @Query("SELECT COUNT(*) FROM Student")
    fun getStudentCount(): Int
//    @Query("SELECT * FROM Student WHERE userId = :userId AND Sname LIKE '%' || :query || '%'")
//    fun searchStudentsByName(userId: String, query: String): List<Student>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudent(student: Student)
    @Update
    fun updateStudent(student: Student)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT * FROM Student WHERE studentID = :studentId")
    suspend fun getStudentById(studentId: Int): Student?
    @Query("SELECT * FROM Student")
    fun getAllStudents(): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStudents(student: List<Student>)

    @Query("SELECT * FROM Student WHERE userId = :userId")
    suspend fun getStudentsByUserId(userId: String): List<Student>
    }


