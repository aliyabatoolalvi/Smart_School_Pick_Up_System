package com.finallab.smartschoolpickupsystem.model.DataModels

data class Feedback(
    val feedbackText: String = "",
    val guardianName: String = "",
    val sentiment: String = "",
    val status: String = "",
    val timestamp: com.google.firebase.Timestamp? = null,
    val guardianUID: String = "",
    val guardianCNIC: String = "",
    val score: Int = 0,
    val intent: String = "",
    val adminReply: String = ""
)
