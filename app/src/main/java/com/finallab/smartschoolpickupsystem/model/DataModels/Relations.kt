package com.finallab.smartschoolpickupsystem.DataModels

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class GuardianWithStudents(
    @Embedded val guardian: Guardian, // This embeds the Guardian object

    @Relation(
        parentColumn = "guardianID",             // Correct column from Guardian
        entityColumn = "studentID",              // Correct column from Student
        associateBy = Junction(GuardianStudentCrossRef::class) // Join table
    )
    val students: List<Student>
)

data class StudentWithGuardians(
    @Embedded val student: Student, // This embeds the Student object

    @Relation(
        parentColumn = "studentID",             // Correct column from Student
        entityColumn = "guardianID",            // Correct column from Guardian
        associateBy = Junction(GuardianStudentCrossRef::class) // Join table
    )
    val guardians: List<Guardian>
)
