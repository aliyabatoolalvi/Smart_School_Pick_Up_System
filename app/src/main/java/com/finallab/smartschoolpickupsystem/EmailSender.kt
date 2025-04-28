package com.finallab.smartschoolpickupsystem

import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType


class EmailSender(private val apiKey: String, private val apiSecret: String) {

    fun sendEmail(to: String, subject: String, message: String, callback: (Boolean, String?) -> Unit) {
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("Messages", listOf(JSONObject().apply {
                put("From", JSONObject().apply {
                    put("Email", "alvialiyabatool@gmail.com")
                    put("Name", "Smart School Pick Up System")
                })
                put("To", listOf(JSONObject().apply {
                    put("Email", to)
                    put("Name", "Recipient")
                }))
                put("Subject", subject)
                put("TextPart", message)
            }))
        }

        val body = RequestBody.create("application/json".toMediaType(), json.toString())
        val request = Request.Builder()
            .url("https://api.mailjet.com/v3.1/send")
            .post(body)
            .addHeader("Authorization", Credentials.basic(apiKey, apiSecret))
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, response.body?.string())
                }
            }
        })
    }
}
