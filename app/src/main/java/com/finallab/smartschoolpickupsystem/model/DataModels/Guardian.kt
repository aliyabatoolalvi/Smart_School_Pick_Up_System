package com.finallab.smartschoolpickupsystem.DataModels

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class Guardian (
    @PrimaryKey(autoGenerate = true)
    var guardianID: Int=0,
    var studentDocumentID : String,
    var Gname: String,
    var number: String,
    var CNIC: String,
    var  Email: String,
    var QRcodeData: String,
    var QRcodeBase64: String,
    var userId: String,
    var studentID : Int,
    var guardianDocId: String = ""


){



}
