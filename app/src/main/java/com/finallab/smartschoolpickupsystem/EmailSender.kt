package com.finallab.smartschoolpickupsystem

import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender(private val email: String, private val password: String) : Thread() {

    private val host = "smtp.gmail.com"

    fun sendEmail(to: String, subject: String, message: String) {
        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", "587")
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(email, password)
            }
        })

        try {
            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(email))
                addRecipient(Message.RecipientType.TO, InternetAddress(to))
                this.subject = subject
                setText(message)
            }

            Transport.send(mimeMessage)
            println("Email sent successfully")
        } catch (e: MessagingException) {
            e.printStackTrace()
        }
    }
}
