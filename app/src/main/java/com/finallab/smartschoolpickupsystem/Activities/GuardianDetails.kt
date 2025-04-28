package com.finallab.smartschoolpickupsystem.Activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.Utilities
import com.finallab.smartschoolpickupsystem.databinding.ActivityGuardianDetailsBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GuardianDetails : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianDetailsBinding
    private var guardian: Guardian? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()

        val guardianId = intent.getIntExtra("guardianID", -1)
        val docId = intent.getStringExtra("guardianDocumentID")

        if (docId != null && Utilities.isNetworkConnected(this)) {
            loadGuardianFromFirestore(docId)
        } else if (guardianId != -1) {
            loadGuardianDetailsFromRoom(guardianId)
        } else {
            showErrorAndExit("No guardian information provided")
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun loadGuardianFromFirestore(docId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("guardians")
            .document(docId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val guardian = Guardian(
                        Gname = document.getString("Gname") ?: "",
                        number = document.getString("number") ?: "",
                        CNIC = document.getString("CNIC") ?: "",
                        Email = document.getString("Email") ?: "",
                        QRcodeBase64 = document.getString("QRcodeBase64") ?: ""
                    )
                    displayGuardianDetails(guardian)
                } else {
                    showToast("Guardian not found in Firestore.")
                }
            }
            .addOnFailureListener { e ->
                showToast("Error fetching guardian: ${e.message}")
            }
    }

    private fun loadGuardianDetailsFromRoom(guardianId: Int) {
        lifecycleScope.launch {
            try {
                val guardianResult = withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(this@GuardianDetails).guardianDao().getGuardianById(guardianId)
                }

                guardian = guardianResult

                guardian?.let {
                    displayGuardianDetails(it)
                } ?: showErrorAndExit("Guardian not found")
            } catch (e: Exception) {
                Log.e("GuardianDetails", "Error fetching guardian: ${e.message}")
                showErrorAndExit("Error: Unable to fetch guardian details")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayGuardianDetails(guardian: Guardian) {
        val formattedCNIC = formatCNIC(guardian.CNIC)
        val formattedPhone = formatPhoneNumber(guardian.number)

        with(binding) {
            textView5.text = "Name: ${guardian.Gname}"
            textView7.text = "Phone: $formattedPhone"
            textView8.text = "CNIC: $formattedCNIC"
            textView9.text = "Email: ${guardian.Email}"
        }

        setupClickableContact(binding.textView7, guardian.number, "tel:")
        setupClickableContact(binding.textView9, guardian.Email, "mailto:")

        loadQRCode(guardian.QRcodeBase64)
    }

    private fun setupClickableContact(textView: TextView, contactInfo: String?, prefix: String) {
        contactInfo?.let {
            textView.setTextColor(ContextCompat.getColor(this, R.color.black))  // 🔥 Force black color
            textView.paint.isUnderlineText = false
            textView.setOnClickListener { openContactIntent(prefix, contactInfo) }
        }
    }

    private fun openContactIntent(prefix: String, contactInfo: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("$prefix$contactInfo")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("GuardianDetails", "Error opening contact intent: ${e.message}")
            showToast("Error: Unable to open contact")
        }
    }

    private fun loadQRCode(qrCodeBase64: String?) {
        if (qrCodeBase64.isNullOrEmpty()) {
            showToast("Error: No QR code found")
            return
        }

        binding.qrLoading.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            val qrCodeBitmap = decodeBase64ToBitmap(qrCodeBase64)

            withContext(Dispatchers.Main) {
                binding.qrLoading.visibility = View.GONE

                if (qrCodeBitmap != null) {
                    binding.qrcode.setImageBitmap(qrCodeBitmap)
                } else {
                    Log.e("GuardianDetails", "Failed to decode QR code")
                    showToast("Error: Invalid QR code data")
                }
            }
        }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            Log.e("GuardianDetails", "Error decoding Base64: ${e.message}")
            null
        }
    }

    private fun formatCNIC(cnic: String?): String {
        return if (!cnic.isNullOrEmpty() && cnic.length == 13) {
            "${cnic.substring(0, 5)}-${cnic.substring(5, 12)}-${cnic.substring(12)}"
        } else {
            cnic ?: ""
        }
    }

    private fun formatPhoneNumber(number: String?): String {
        return if (!number.isNullOrEmpty() && number.length == 11 && number.startsWith("03")) {
            "${number.substring(0, 4)}-${number.substring(4)}"
        } else {
            number ?: ""
        }
    }

    private fun showErrorAndExit(message: String) {
        showToast(message)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
