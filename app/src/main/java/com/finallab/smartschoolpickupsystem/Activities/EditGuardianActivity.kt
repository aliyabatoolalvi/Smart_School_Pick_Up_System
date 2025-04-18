package com.finallab.smartschoolpickupsystem.Activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityEditGuardianBinding
import com.finallab.smartschoolpickupsystem.model.Repository.GuardianStudentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class EditGuardianActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditGuardianBinding
    private var guardian: Guardian? = null
    private lateinit var repository: GuardianStudentRepository
    private lateinit var firestore: FirebaseFirestore

    private var guardianId: Int = -1
    private var guardianDocId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditGuardianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        repository = GuardianStudentRepository(database)
        firestore = FirebaseFirestore.getInstance()

        guardianId = intent.getIntExtra("guardianID", -1)
        guardianDocId = intent.getStringExtra("guardianDocumentID") ?: ""

        if (guardianId == -1 || guardianDocId.isEmpty()) {
            showToast("Invalid guardian ID or document ID")
            finish()
            return
        }

        loadGuardianData()

        binding.upG.setOnClickListener { updateGuardian() }
        binding.cancel.setOnClickListener { finish() }
    }

    private fun loadGuardianData() {
        firestore.collection("guardians").document(guardianDocId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val guardian = document.toObject(Guardian::class.java)

                    binding.nameG.setText(guardian?.Gname ?: "")
                    binding.cnic.setText(guardian?.CNIC ?: "")
                    binding.num.setText(guardian?.number ?: "")
                    binding.email.setText(guardian?.Email ?: "")

                } else {
                    showToast("Guardian not found in Firestore")
                    finish()
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to fetch guardian: ${e.message}")
                finish()
            }
    }


    private fun updateGuardian() {
        val updatedName = binding.nameG.text.toString().trim()
        val updatedCNIC = binding.cnic.text.toString().trim()
        val updatedPhone = binding.num.text.toString().trim()
        val updatedEmail = binding.email.text.toString().trim()

        if (updatedName.isEmpty() || updatedCNIC.isEmpty() || updatedPhone.isEmpty()) {
            showToast("All fields are required")
            return
        }

        // Create updated data map for Firestore
        val updatedData = mapOf(
            "Gname" to updatedName,
            "CNIC" to updatedCNIC,
            "number" to updatedPhone,
            "Email" to updatedEmail
        )

        // Update in Firestore first
        firestore.collection("guardians").document(guardianDocId)
            .update(updatedData)
            .addOnSuccessListener {
                showToast("Guardian updated in Firestore")

                // Now update in Room (if it exists)
                lifecycleScope.launch {
                    guardian = repository.getGuardianById(guardianId)
                    guardian?.let {
                        val updatedGuardian = it.copy(
                            Gname = updatedName,
                            CNIC = updatedCNIC,
                            number = updatedPhone,
                            Email = updatedEmail
                        )
                        repository.updateGuardian(updatedGuardian)
                        showToast("Guardian updated in local Room database")
                    }
                    finish()
                }

            }
            .addOnFailureListener { e ->
                showToast("Failed to update Firestore: ${e.message}")
            }
    }


    private fun updateFirestoreGuardian(docId: String, guardian: Guardian) {
        firestore.collection("guardians").document(docId)
            .update(
                "Gname", guardian.Gname,
                "CNIC", guardian.CNIC,
                "number", guardian.number,
                "Email", guardian.Email

            )
            .addOnSuccessListener {
                showToast("Success to update Firestore")
            }
            .addOnFailureListener { e ->
                showToast("Failed to update Firestore: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
