package com.finallab.smartschoolpickupsystem.DataModels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.common.base.Converter

@Entity
data class Student (
//    @PrimaryKey(autoGenerate = true)
//    var id: Int = 0,  // Local database ID (Room)

    @PrimaryKey@ColumnInfo(name = "studentID")
    var studentID: Int = 0,

    var Sname: String,
    var reg: String,
    var studentClass: String,
    var section: String,
    var firestoreId: String = "",
    var userId: String,
    var studentDocId: String = "",

    @TypeConverters(Converter::class)
    val guardians: List<String> = emptyList()

) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "studentID" to studentID,
            "Sname" to Sname,
            "reg" to reg,
            "studentClass" to studentClass,
            "section" to section,
            "firestoreId" to firestoreId,
            "userId" to userId,
            "studentDocId" to studentDocId,
            "guardians" to guardians
        )
    }
}
