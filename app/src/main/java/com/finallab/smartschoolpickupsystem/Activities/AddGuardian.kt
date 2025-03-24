package com.finallab.smartschoolpickupsystem.Activities

import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*


class AddGuardian : AppCompatActivity() {

    private lateinit var binding: ActivityAddGuardianBinding
    private val firestore = FirebaseFirestore.getInstance()

    // Initialize the ViewModel using the Factory for the repository
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
        val name = binding.Gname.editText?.text.toString()
        val number = binding.number.editText?.text.toString()
        val cnic = binding.CNIC.editText?.text.toString()
        val email = binding.Email.editText?.text.toString()

        return when {
            name.isEmpty() || number.isEmpty() || cnic.isEmpty() || email.isEmpty() -> {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                false
            }

            cnic.length != 13 || !cnic.all { it.isDigit() } -> {
                binding.CNIC.error = "CNIC must be 13 digits only"
                false
            }

            number.length != 11 || !number.all { it.isDigit() } -> {
                binding.number.error = "Invalid phone number"
                false
            }

            else -> true
        }
    }

    /**
     * Save guardian data along with student document ID.
     */
    private fun saveGuardianWithStudentDocID(studentID: Int, studentDocID: String, qrData: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val guardian = Guardian(
            studentID = studentID,
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
    }


    /**
     * Generate a QR code and convert it to Base64.
     */
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

    /**
     * Save guardian data to Firestore.
     */
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
            "userId" to userId
        )

        firestore.collection("guardians")
            .add(guardianMap)
            .addOnSuccessListener { documentReference ->
                // Set the Firestore document ID
                guardian.guardianDocId = documentReference.id

                // Save updated guardian to local Room database
                saveGuardianToLocalDatabase(guardian)

                Toast.makeText(
                    this,
                    "Guardian Registered Successfully in Firestore",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Save guardian data to the local Room database.
     */
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

    /**
     * Check for duplicate CNIC in Firestore.
     */
    private fun checkForDuplicateCNIC(cnic: String, callback: (Boolean) -> Unit) {
        firestore.collection("guardians")
            .whereEqualTo("CNIC", cnic)
            .get()
            .addOnSuccessListener { querySnapshot ->
                callback(!querySnapshot.isEmpty) // Returns true if duplicate exists
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error checking CNIC: ${e.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }



    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

}




