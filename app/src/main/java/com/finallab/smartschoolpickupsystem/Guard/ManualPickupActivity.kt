package com.finallab.smartschoolpickupsystem.Guard

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.finallab.smartschoolpickupsystem.databinding.ActivityManualPickupBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
class ManualPickupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManualPickupBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var guardEmail: String
    private lateinit var guardName: String
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualPickupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        guardEmail = intent.getStringExtra("guardEmail") ?: ""
        guardName = intent.getStringExtra("guardName") ?: ""

        progressDialog = ProgressDialog(this).apply {
            setMessage("Verifying CNIC...")
            setCancelable(false)
        }

        binding.btnSearch.setOnClickListener {
            val cnic = binding.etCnic.editText?.text.toString().trim()
            if (cnic.length != 13 || !cnic.matches(Regex("^\\d{13}$"))) {
                ToastUtil.showToast(this, "Enter valid 13-digit CNIC")
            } else {
                findGuardianAndStudentsByCnic(cnic)
            }
        }
    }

    private fun findGuardianAndStudentsByCnic(cnic: String) {
        progressDialog.show()

        firestore.collection("guardians")
            .whereEqualTo("CNIC", cnic)
            .get()
            .addOnSuccessListener { docs ->
                progressDialog.dismiss()

                if (docs.isEmpty) {
                    ToastUtil.showToast(this, "Guardian not found")
                    return@addOnSuccessListener
                }

                val doc = docs.documents.first()
                val studentIds = doc.get("students") as? ArrayList<String> ?: arrayListOf()
                val guardianName = doc.getString("Gname") ?: "Guardian"

                if (studentIds.isEmpty()) {
                    ToastUtil.showToast(this, "No students linked to this CNIC")
                    return@addOnSuccessListener
                }

                val guardianId = doc.id
                val intent = Intent(this, ScannedStudentListActivity::class.java)
                intent.putStringArrayListExtra("studentIds", studentIds)
                intent.putExtra("guardianName", guardianName)
                intent.putExtra("guardianId", guardianId)
                intent.putExtra("guardName", guardName)
                intent.putExtra("guardEmail", guardEmail)
                startActivity(intent)
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                ToastUtil.showToast(this, "Lookup failed")
            }
    }
}
