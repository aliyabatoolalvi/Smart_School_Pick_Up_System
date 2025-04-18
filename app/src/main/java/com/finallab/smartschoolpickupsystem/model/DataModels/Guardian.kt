package com.finallab.smartschoolpickupsystem.DataModels

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Guardian(
    @PrimaryKey(autoGenerate = true)
    var guardianID: Int = 0,
    var studentDocumentID: String = "",
    var Gname: String = "",
    var number: String = "",
    var CNIC: String = "",
    var Email: String = "",
    var QRcodeData: String = "",
    var QRcodeBase64: String = "",
    var userId: String = "",
    var guardianDocId: String = ""
)




private fun Guardian.toMap(): Map<String, Any?> {
    return mapOf(
        "guardianID" to guardianID,
        "Gname" to Gname,
        "number" to number,
        "CNIC" to CNIC,
        "Email" to Email,
        "QRcodeData" to QRcodeData,
        "QRcodeBase64" to QRcodeBase64,
        "studentDocumentID" to studentDocumentID,
        "userId" to userId,
        "guardianDocId" to guardianDocId
    )
}


