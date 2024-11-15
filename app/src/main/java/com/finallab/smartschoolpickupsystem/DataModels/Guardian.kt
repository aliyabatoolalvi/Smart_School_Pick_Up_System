package com.finallab.smartschoolpickupsystem.DataModels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Guardian (
    @PrimaryKey(autoGenerate = true)
    var id: Int=0,
    var studentID:Int,
    var Gname: String,
    var number: String,
    var CNIC: String,
    var  Email: String,
    var QRcodeData: String
)