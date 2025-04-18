package com.finallab.smartschoolpickupsystem.DataModels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.common.base.Converter

@Entity
data class Student (
    @PrimaryKey(autoGenerate = true)@ColumnInfo(name = "studentID")
    var studentID: Int = 0,
    var Sname: String,
    var reg: String,
    var studentClass: String,
    var section: String,
    var userId: String,
    var studentDocId: String = "",
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
