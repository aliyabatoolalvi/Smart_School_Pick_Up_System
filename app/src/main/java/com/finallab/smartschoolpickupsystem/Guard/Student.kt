package com.finallab.smartschoolpickupsystem.Guard

data class Student(
    var studentID: Int = 0,                // For Room (optional if you use Firestore only)
    var Sname: String = "",                // Student Name
    var reg: String = "",                  // Registration Number
    var studentClass: String = "",         // Class
    var section: String = "",              // Section
    var userId: String = "",               // School admin ID
    var studentDocId: String = ""          // Firestore Document ID
)