package com.finallab.smartschoolpickupsystem.Activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityAddGuardianBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.util.*

class AddGuardian : AppCompatActivity() {

    private lateinit var binding: ActivityAddGuardianBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var guardianDbHelper: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGuardianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        guardianDbHelper = AppDatabase.getDatabase(this)

        val studentID = intent.getIntExtra("id", -1)
        val studentDocID = intent.getStringExtra("studentDocumentID") ?: ""
//        if (studentDocID.isNullOrEmpty() || studentDocID == "n") {
//            Toast.makeText(this, "Invalid Student Document ID", Toast.LENGTH_SHORT).show()
//            return
//        }

        binding.regG.setOnClickListener {
            if (validateInputs()) {
                val cnic = binding.CNIC.editText?.text.toString()
                checkForDuplicateCNIC(cnic) { isDuplicate ->
                    if (isDuplicate) {
                        binding.CNIC.error = "CNIC already exists"
                        Toast.makeText(this, "Guardian with this CNIC already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        val qrData = UUID.randomUUID().toString() // Generate unique QR code data
                        saveGuardianWithStudentDocID(studentID, studentDocID, qrData)
                    }
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        return when {
            binding.Gname.editText?.text.toString().isEmpty() ||
                    binding.number.editText?.text.toString().isEmpty() ||
                    binding.CNIC.editText?.text.toString().isEmpty() ||
                    binding.Email.editText?.text.toString().isEmpty() -> {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                false
            }
            binding.CNIC.editText?.text.toString().length != 13 ||
                    !binding.CNIC.editText?.text.toString().all { it.isDigit() } -> {
                binding.CNIC.error = "CNIC must be 13 digits only"
                false
            }
            binding.number.editText?.text.toString().length != 11 ||
                    !binding.number.editText?.text.toString().all { it.isDigit() } -> {
                binding.number.error = "Invalid phone number"
                false
            }
            else -> true
        }
    }

    private fun saveGuardianWithStudentDocID(studentID: Int, studentDocID: String, qrData: String) {
        val guardian = Guardian(
            studentID = studentID, // The studentID remains in the guardian
            studentDocumentID = studentDocID, // Directly store the passed studentDocID
            Gname = binding.Gname.editText?.text.toString(),
            number = binding.number.editText?.text.toString(),
            CNIC = binding.CNIC.editText?.text.toString(),
            Email = binding.Email.editText?.text.toString(),
            QRcodeData = qrData, // QR code data is unique and used for linking
            QRcodeBase64 = generateQRCodeBase64(qrData) // Optionally store QR code base64 if needed
        )

        saveGuardianToFirestore(guardian)
    }

    private fun generateQRCodeBase64(qrData: String): String {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(qrData, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400)
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
            "studentID" to guardian.studentID,
            "studentDocumentID" to guardian.studentDocumentID,
            "Gname" to guardian.Gname,
            "number" to guardian.number,
            "CNIC" to guardian.CNIC,
            "Email" to guardian.Email,
            "QRcodeData" to guardian.QRcodeData,
            "QRcodeBase64" to guardian.QRcodeBase64,
            "userId" to userId  // Add userId field
        )

        firestore.collection("guardians")
            .add(guardianMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Guardian Registered Successfully in Firestore", Toast.LENGTH_SHORT).show()
                saveGuardianToLocalDatabase(guardian)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveGuardianToLocalDatabase(guardian: Guardian) {
        lifecycleScope.launch(Dispatchers.IO) {
            guardianDbHelper.guardianDao().insert(guardian)
            runOnUiThread {
                Toast.makeText(this@AddGuardian, "Guardian Registered Successfully in Local DB", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun checkForDuplicateCNIC(cnic: String, callback: (Boolean) -> Unit) {
        firestore.collection("guardians")
            .whereEqualTo("CNIC", cnic)
            .get()
            .addOnSuccessListener { querySnapshot ->
                callback(!querySnapshot.isEmpty)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error checking CNIC: ${e.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }
}
