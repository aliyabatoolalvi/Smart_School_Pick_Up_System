package com.finallab.smartschoolpickupsystem.DataModels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Student(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "studentID")
    var studentID: Int = 0,

    var Sname: String = "",
    var reg: String = "",
    var studentClass: String = "",
    var section: String = "",
    var userId: String = "",              // added by logged-in school
    var studentDocId: String = ""         // Firestore document ID
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "studentID" to studentID,
            "Sname" to Sname,
            "reg" to reg,
            "studentClass" to studentClass,
            "section" to section,
            "userId" to userId,
            "studentDocId" to studentDocId,

        )
    }
}
