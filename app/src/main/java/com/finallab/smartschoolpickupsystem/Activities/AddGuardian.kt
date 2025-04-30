package com.finallab.smartschoolpickupsystem.Activities

import EmailSender
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.finallab.smartschoolpickupsystem.BuildConfig
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.Utilities.isNetworkConnected
import com.finallab.smartschoolpickupsystem.ViewModel.GuardianStudentViewModel
import com.finallab.smartschoolpickupsystem.ViewModel.GuardianStudentViewModelFactory
import com.finallab.smartschoolpickupsystem.databinding.ActivityAddGuardianBinding
import com.finallab.smartschoolpickupsystem.model.Repository.GuardianStudentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*

class AddGuardian : AppCompatActivity() {

    private lateinit var binding: ActivityAddGuardianBinding
    private val firestore = FirebaseFirestore.getInstance()

    private val viewModel: GuardianStudentViewModel by viewModels {
        GuardianStudentViewModelFactory(GuardianStudentRepository(AppDatabase.getDatabase(this)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGuardianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val studentDocID = intent.getStringExtra("studentDocumentID") ?: ""
        binding.backButton.setOnClickListener { onBackPressed() }

        binding.regG.setOnClickListener {
            lifecycleScope.launch {
                if (validateInputs()) {
                    val cnic = binding.CNIC.editText?.text.toString().trim()
                    if (isNetworkConnected(this@AddGuardian)) {
                        try {
                            val snapshot = firestore.collection("guardians")
                                .whereEqualTo("CNIC", cnic)
                                .get()
                                .await()

                            if (!snapshot.isEmpty) {
                                showToast("Guardian with this CNIC already exists!")
                                binding.regG.isEnabled = true
                                binding.progressBar.visibility = View.GONE
                            } else {
                                val qrData = UUID.randomUUID().toString()

                                val guardianEmail = binding.Email.editText?.text.toString()
                                val guardianName = binding.Gname.editText?.text.toString()
                                val generatedPassword = generateRandomPassword(8)

                                createGuardianAuthAccount(studentDocID, qrData, guardianEmail, guardianName, generatedPassword)
                            }
                        } catch (e: Exception) {
                            showToast("Error checking CNIC: ${e.message}")
                            binding.regG.isEnabled = true
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }

    }

    private fun generateRandomPassword(length: Int = 8): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#"
        return (1..length).map { chars.random() }.joinToString("")
    }


    private fun saveGuardian(studentDocId: String, qrData: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val guardian = Guardian(
            Gname = binding.Gname.editText?.text.toString(),
            number = binding.number.editText?.text.toString(),
            CNIC = binding.CNIC.editText?.text.toString(),
            Email = binding.Email.editText?.text.toString(),
            QRcodeData = qrData,
            QRcodeBase64 = generateQRCodeBase64(qrData),
            userId = userId
        )

        binding.regG.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("guardians")
            .add(guardian.toMap())
            .addOnSuccessListener { documentReference ->
                documentReference.update("guardianDocId", documentReference.id)
                    .addOnSuccessListener {
                        guardian.guardianDocId = documentReference.id
                        saveGuardianToLocalDatabase(guardian)


                        showToast("Guardian saved successfully!")
                        setResult(RESULT_OK)
                        binding.progressBar.visibility = View.GONE
                        finish()
                    }
                    .addOnFailureListener { e ->
                        showToast("Failed to update guardianDocId: ${e.message}")
                        binding.regG.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                    }
            }
            .addOnFailureListener { e ->
                showToast("Error saving guardian: ${e.message}")
                binding.regG.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
    }

//    private fun linkGuardianToStudent(guardianDocId: String, studentDocId: String) {
//        if (guardianDocId.isEmpty() || studentDocId.isEmpty()) {
//            showToast("Guardian or Student ID missing, cannot link.")
//            return
//        }
//
//        val guardianRef = firestore.collection("guardians").document(guardianDocId)
//        val studentRef = firestore.collection("students").document(studentDocId)
//
//        guardianRef.get()
//            .addOnSuccessListener { guardianSnapshot ->
//                if (guardianSnapshot.exists()) {
//                    firestore.runTransaction { transaction ->
//                        val guardianDoc = transaction.get(guardianRef)
//                        val studentDoc = transaction.get(studentRef)
//
//                        val currentStudents = guardianDoc.get("students") as? List<String> ?: emptyList()
//                        transaction.update(guardianRef, "students", currentStudents + studentDocId)
//
//                        val currentGuardians = studentDoc.get("guardians") as? List<String> ?: emptyList()
//                        transaction.update(studentRef, "guardians", currentGuardians + guardianDocId)
//                    }
//                        .addOnSuccessListener {
//                            showToast("Guardian linked to student successfully!")
//                        }
//                        .addOnFailureListener { e ->
//                            showToast("Error linking guardian: ${e.message}")
//                        }
//                } else {
//                    showToast("Guardian not found. Cannot link.")
//                }
//            }
//            .addOnFailureListener { e ->
//                showToast("Failed to fetch guardian: ${e.message}")
//            }
//    }

    private fun saveGuardianToLocalDatabase(guardian: Guardian) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.insertGuardian(guardian)
            withContext(Dispatchers.Main) {
                // No need to show Toast here separately now
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.Gname.editText?.text.toString().trim()
        val number = binding.number.editText?.text.toString().trim()
        val cnic = binding.CNIC.editText?.text.toString().trim()
        val email = binding.Email.editText?.text.toString().trim()

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        val cnicRegex = "^[0-9]{13}$".toRegex()
        val phoneRegex = "^(03[0-9]{9}|\\+?[1-9][0-9]{9,14})$".toRegex()

        return when {
            name.isEmpty() || number.isEmpty() || cnic.isEmpty() || email.isEmpty() -> {
                showToast("Please fill in all fields")
                false
            }
            !cnic.matches(cnicRegex) -> {
                binding.CNIC.error = "CNIC must be exactly 13 digits"
                false
            }
            !number.matches(phoneRegex) -> {
                binding.number.error = "Invalid phone number"
                false
            }
            !email.matches(emailRegex) -> {
                binding.Email.error = "Invalid email format"
                false
            }
            else -> true
        }
    }

    private fun generateQRCodeBase64(qrData: String): String {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(
                qrData,
                com.google.zxing.BarcodeFormat.QR_CODE,
                400,
                400
            )
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            ""
        }
    }

    private fun createGuardianAuthAccount(
        studentDocId: String,
        qrData: String,
        email: String,
        name: String,
        password: String
    ) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        val sharedPref = getSharedPreferences("AdminPrefs", MODE_PRIVATE)
        val adminEmail = sharedPref.getString("admin_email", null)
        val adminPassword = sharedPref.getString("admin_password", null)

        if (adminEmail.isNullOrEmpty() || adminPassword.isNullOrEmpty()) {
            showToast("Admin credentials not found. Please login again.")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val guardianUid = authResult.user?.uid
                if (guardianUid != null) {
                    FirebaseAuth.getInstance().signOut()

                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(adminEmail, adminPassword)
                        .addOnSuccessListener {
                            try {
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                if (currentUser == null) {
                                    showToast("Failed to re-login admin.")
                                    return@addOnSuccessListener
                                }

                                val currentSchoolUid = currentUser.uid
                                val guardian = Guardian(
                                    Gname = name,
                                    number = binding.number.editText?.text.toString(),
                                    CNIC = binding.CNIC.editText?.text.toString(),
                                    Email = email,
                                    QRcodeData = qrData,
                                    QRcodeBase64 = generateQRCodeBase64(qrData),
                                    userId = currentSchoolUid
                                )

                                firestore.collection("guardians")
                                    .document(guardianUid)
                                    .set(guardian.toMap())
                                    .addOnSuccessListener {
                                        val userMap = mapOf(
                                            "uid" to guardianUid,
                                            "email" to email,
                                            "role" to "guardian",
                                            "schoolId" to currentSchoolUid
                                        )

                                        firestore.collection("users")
                                            .document(guardianUid)
                                            .set(userMap)
                                            .addOnSuccessListener {
                                                saveGuardianToLocalDatabase(guardian)
                                                sendGuardianEmail(email, password)
                                                showToast("Guardian registered successfully!")
                                                binding.progressBar.visibility = View.GONE
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("Firestore", "Failed to save user info", e)
                                                showToast("Failed to save user info: ${e.message}")
                                                binding.progressBar.visibility = View.GONE
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Failed to save guardian", e)
                                        showToast("Failed to save guardian data: ${e.message}")
                                        binding.progressBar.visibility = View.GONE
                                    }
                            } catch (e: Exception) {
                                Log.e("ReLogin", "Admin re-login crash: ${e.message}", e)
                                showToast("Unexpected error: ${e.message}")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ReLogin", "Firebase sign-in failed: ${e.message}", e)
                            showToast("Admin re-login failed: ${e.message}")
                            binding.progressBar.visibility = View.GONE
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("GuardianAccount", "Failed to create guardian: ${e.message}", e)
                if (e.message?.contains("email address is already in use") == true) {
                    showToast("Guardian email is already registered!")
                } else {
                    showToast("Failed to create Guardian account: ${e.message}")
                }
                binding.progressBar.visibility = View.GONE
            }
    }


    private fun sendGuardianEmail(email: String, password: String) {
        val sender = EmailSender(BuildConfig.MAILJET_API_KEY, BuildConfig.MAILJET_SECRET_KEY)

        val subject = "Smart School Pickup - Your Login Credentials"
        val message = """
        Dear Guardian,

        Your account has been created successfully.

        Here are your login details:

        Email: $email
        Password: $password

        Please keep this information safe.

        Regards,
        Smart School Pickup Team
    """.trimIndent()

        sender.sendEmail(email, subject, message) { success, error ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Email sent successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("MailjetEmail", "Failed to send email: $error")
                    Toast.makeText(this, "Failed to send email: $error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }



    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
