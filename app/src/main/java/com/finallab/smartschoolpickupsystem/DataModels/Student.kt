package com.finallab.smartschoolpickupsystem.DataModels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Student (
    @PrimaryKey(autoGenerate = true)
    var id: Int=0,
    var Sname: String,
    var reg: String,
    var studentClass: String,
    var section: String,
    var studentDocId: String

    ){

}