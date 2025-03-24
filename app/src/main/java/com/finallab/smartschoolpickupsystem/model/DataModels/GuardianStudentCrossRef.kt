package com.finallab.smartschoolpickupsystem.DataModels

import androidx.room.Entity

@Entity(primaryKeys = ["guardianID", "studentID"])
data class GuardianStudentCrossRef(
    val guardianID: Int,
    val studentID: Int
)
