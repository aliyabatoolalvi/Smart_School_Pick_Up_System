package com.finallab.smartschoolpickupsystem.Activities

import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.EmailSender
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.Utilities.isNetworkConnected
import com.finallab.smartschoolpickupsystem.ViewModel.GuardianStudentViewModel
import com.finallab.smartschoolpickupsystem.ViewModel.GuardianStudentViewModelFactory
import com.finallab.smartschoolpickupsystem.databinding.ActivityAddGuardianBinding
import com.finallab.smartschoolpickupsystem.model.Repository.GuardianStudentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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

        // Ensure user authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT)
                .show()
            finish()
            return
        }

        // Retrieve Intent extras
        val studentID = intent.getIntExtra("id", -1)
        val studentDocID = intent.getStringExtra("studentDocumentID") ?: ""

        binding.regG.setOnClickListener {
            if (validateInputs()) {
                val cnic = binding.CNIC.editText?.text.toString()
                checkForDuplicateCNIC(cnic) { isDuplicate ->
                    if (isDuplicate) {
                        binding.CNIC.error = "CNIC already exists"
                        Toast.makeText(
                            this,
                            "Guardian with this CNIC already exists",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val qrData = UUID.randomUUID().toString()
                        saveGuardianWithStudentDocID(studentID, studentDocID, qrData)

                    }
                }
            }
        }
    }

    /**
     * Validate user input fields.
     */
    private fun validateInputs(): Boolean {
        val name = binding.Gname.editText?.text.toString().trim()
        val number = binding.number.editText?.text.toString().trim()
        val cnic = binding.CNIC.editText?.text.toString().trim()
        val email = binding.Email.editText?.text.toString().trim()

        // Regex patterns
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        val cnicRegex = "^[0-9]{13}$".toRegex()

        // Accepts both Pakistani and international formats:
        val phoneRegex = "^(03[0-9]{9}|\\+?[1-9][0-9]{9,14})$".toRegex()

        return when {
            name.isEmpty() || number.isEmpty() || cnic.isEmpty() || email.isEmpty() -> {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                false
            }

            !cnic.matches(cnicRegex) -> {
                binding.CNIC.error = "CNIC must be exactly 13 digits (e.g., 1234512345678)"
                false
            }

            !number.matches(phoneRegex) -> {
                binding.number.error =
                    "Phone number must be a valid Pakistani (03XXXXXXXXX) or international number (e.g., +12345678901)"
                false
            }

            !email.matches(emailRegex) -> {
                binding.Email.error = "Invalid email format (e.g., example@domain.com)"
                false
            }

            else -> true
        }
    }

    private fun saveGuardianWithStudentDocID(studentID: Int, studentDocID: String, qrData: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val guardian = Guardian(
            studentDocumentID = studentDocID,
            Gname = binding.Gname.editText?.text.toString(),
            number = binding.number.editText?.text.toString(),
            CNIC = binding.CNIC.editText?.text.toString(),
            Email = binding.Email.editText?.text.toString(),
            QRcodeData = qrData,
            QRcodeBase64 = generateQRCodeBase64(qrData),
            userId = userId
        )

        saveGuardianToFirestore(guardian)
        sendGuardianEmail(guardian.Email,generateRandomPassword(8))
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

    private fun saveGuardianToFirestore(guardian: Guardian) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val guardianMap = hashMapOf(
            "studentDocumentID" to guardian.studentDocumentID,
            "Gname" to guardian.Gname,
            "number" to guardian.number,
            "CNIC" to guardian.CNIC,
            "Email" to guardian.Email,
            "QRcodeData" to guardian.QRcodeData,
            "QRcodeBase64" to guardian.QRcodeBase64,
            "userId" to userId
        )

        firestore.collection("guardians")
            .add(guardianMap)
            .addOnSuccessListener { documentReference ->
                guardian.guardianDocId = documentReference.id
                saveGuardianToLocalDatabase(guardian)

                Toast.makeText(
                    this,
                    "Guardian Registered Successfully with ID: ${documentReference.id}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveGuardianToLocalDatabase(guardian: Guardian) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.insertGuardian(guardian)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@AddGuardian,
                    "Guardian Registered Successfully in Local DB",
                    Toast.LENGTH_SHORT
                ).show()

                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun checkForDuplicateCNIC(cnic: String, callback: (Boolean) -> Unit) {
        if (isNetworkConnected(this)) {
            // Check Firestore if online
            firestore.collection("guardians")
                .whereEqualTo("CNIC", cnic)
                .get()
                .addOnSuccessListener { snapshot ->
                    callback(!snapshot.isEmpty) // True if duplicate exists
                }
                .addOnFailureListener {
                    showToast("Error checking CNIC online: ${it.message}")
                    callback(false)
                }
        } else {
            // Check Room if offline
            lifecycleScope.launch(Dispatchers.IO) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val localGuardians = viewModel.getGuardiansByUserId(userId)
                val isDuplicate = localGuardians.any { it.CNIC == cnic }
                withContext(Dispatchers.Main) {
                    callback(isDuplicate)
                }
            }
        }
    }
    private fun generateRandomPassword(length: Int = 8): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#"
        return (1..length).map { chars.random() }.joinToString("")
    }
    private fun addGuardianAndLinkToStudent(guardian: Guardian, studentDocId: String) {
        // Step 1: Create guardian in Firebase Authentication

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(guardian.Email, generateRandomPassword())
            .addOnSuccessListener { authResult ->
                val guardianUserId = authResult.user?.uid

                // Step 2: Store basic info in 'users' collection
                val userData = mapOf(
                    "schoolID" to FirebaseAuth.getInstance().currentUser?.uid,
                    "guardianUserId" to guardianUserId,
                    "email" to guardian.Email,
                    "role" to "guardian"
                )
                firestore.collection("users").document(guardianUserId!!)
                    .set(userData)
                    .addOnSuccessListener {
                        // Step 3: Store guardian-specific data in 'guardians' collection
                        val guardianData = mapOf(
                            "name" to guardian.Gname,
                            "phone" to guardian.number,
                            "cnic" to guardian.CNIC,
                            "students" to arrayListOf(studentDocId)
                        )

                        firestore.collection("guardians").document(guardianUserId)
                            .set(guardianData)
                            .addOnSuccessListener {
                                // Step 4: Link guardian to the student
                                linkGuardianToStudent(guardianUserId, studentDocId)
                                Toast.makeText(this, "Guardian linked successfully!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to store guardian info", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to store user info", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Guardian registration failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun linkGuardianToStudent(guardianUserId: String, studentDocId: String) {
        firestore.collection("students").document(studentDocId)
            .update("guardians", FieldValue.arrayUnion(guardianUserId))
            .addOnSuccessListener {
                Toast.makeText(this, "Guardian linked to student!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to link guardian to student", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendGuardianEmail(guardianEmail: String, guardianPassword: String) {
        val senderEmail = "yasnaishereandthere@gmail.com"
        val senderPassword = "muff xtml lnep fygd\n"

        val subject = "Your Guardian Account Credentials"
        val message = """
        Dear Guardian,

        Your account has been created successfully.

        Login details:
        Email: $guardianEmail
        Password: $guardianPassword

        Please log in and change your password.

        Regards,
        School Admin
    """.trimIndent()

        // Run in a background thread
        Thread {
            EmailSender(senderEmail, senderPassword).sendEmail(guardianEmail, subject, message)
        }.start()
    }




    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

}




