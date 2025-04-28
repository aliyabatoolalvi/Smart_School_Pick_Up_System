package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
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
                                saveGuardian(studentDocID, qrData)
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

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
