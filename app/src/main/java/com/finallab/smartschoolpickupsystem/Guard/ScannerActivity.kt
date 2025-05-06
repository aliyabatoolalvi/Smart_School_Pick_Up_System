package com.finallab.smartschoolpickupsystem.Guard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finallab.smartschoolpickupsystem.databinding.ActivityScannerBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult

class ScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val phone = intent.getStringExtra("guardPhone")

        if (phone != null) {
            FirebaseFirestore.getInstance().collection("guards")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val name = snapshot.documents[0].getString("name") ?: "Unknown Guard"
                        binding.guardname.text = name.uppercase()
                    } else {
                        binding.guardname.text = "Guard not found"
                    }
                }
                .addOnFailureListener {
                    binding.guardname.text = "Error fetching name"
                }
        } else {
            binding.guardname.text = "No phone provided"
        }
        registerUiListener()
    }

    private fun registerUiListener() {
        binding.sb.setOnClickListener {
            scannerLauncher.launch(
                ScanOptions()
                    .setPrompt("Scan QR Code")
                    .setOrientationLocked(true)
                    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    .setCaptureActivity(PortraitCaptureActivity::class.java)
            )
        }
    }

    private val scannerLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (!result.contents.isNullOrBlank()) {
            val qrCodeValue = result.contents.trim()
            Log.d("ScannerDebug", "Scanned QRcodeData: $qrCodeValue")

            // Launch student list screen
            val intent = Intent(this, ScannedStudentListActivity::class.java)
            intent.putExtra("qrCodeValue", qrCodeValue)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Scan cancelled or invalid", Toast.LENGTH_SHORT).show()
        }
    }
}





