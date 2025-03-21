package com.finallab.smartschoolpickupsystem.Room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.finallab.smartschoolpickupsystem.DataModels.Student

@Dao
interface StudentDAO {
    @Insert
    fun insert(student: Student)

    @Delete
    fun delete(student: Student)

    @Query("Select * from Student")
    fun getAllStudents(): List<Student>

    @Query("SELECT * FROM Student WHERE userId = :userId")
        fun getStudentsByUserId(userId: String): List<Student>




    @Query("Select * from Student where id=:id")
    fun getstudentById(id: Int): Student

    @Update
    fun update(student: Student)

}