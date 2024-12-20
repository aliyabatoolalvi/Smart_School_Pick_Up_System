package com.finallab.smartschoolpickupsystem.Activities


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityGuardianDetailsBinding

class GuardianDetails : AppCompatActivity() {
    lateinit var binding: ActivityGuardianDetailsBinding
    lateinit var guardian: Guardian

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val backButton: ImageButton = binding.backButton
        backButton.setOnClickListener {
            onBackPressed()
        }

        val id = intent.getIntExtra("id", -1)
        if (id == -1) {
            Log.e("GuardianDetails", "Invalid guardian ID")
            showToast("Error: Invalid Guardian ID")
            finish()
            return
        }

        try {
            guardian = AppDatabase.getDatabase(this).guardianDao().getguardianById(id)
        } catch (e: Exception) {
            Log.e("GuardianDetails", "Error fetching guardian from the database: ${e.message}")
            showToast("Error: Unable to fetch guardian details")
            finish()
            return
        }

        binding.textView5.text = "Name: \n" + guardian.Gname
        binding.textView7.text = "Phone: \n" + guardian.number
        binding.textView8.text = "CNIC: \n" + guardian.CNIC
        binding.textView9.text = "Email: \n" + guardian.Email

        val phoneTextView: TextView = binding.textView7
        val emailTextView: TextView = binding.textView9

        phoneTextView.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${guardian.number}")
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("GuardianDetails", "Error initiating phone dial: ${e.message}")
                showToast("Error: Unable to initiate phone dial")
            }
        }

        emailTextView.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:${guardian.Email}")
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("GuardianDetails", "Error initiating email: ${e.message}")
                showToast("Error: Unable to initiate email")
            }
        }

        phoneTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
        emailTextView.setTextColor(ContextCompat.getColor(this, R.color.black))

        val qrCodeBase64 = guardian.QRcodeBase64
        if (!qrCodeBase64.isNullOrEmpty()) {
            val qrCodeBitmap = decodeBase64ToBitmap(qrCodeBase64)
            if (qrCodeBitmap != null) {
                binding.guardianQrCode.setImageBitmap(qrCodeBitmap)
            } else {
                Log.e("GuardianDetails", "Failed to decode QR code")
                showToast("Error: Invalid QR code data")
            }
        } else {
            Log.e("GuardianDetails", "QR code data is empty")
            showToast("Error: No QR code found")
        }
        binding.backButton.setOnClickListener{
            super.onBackPressed()
        }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedByteArray: ByteArray = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
        } catch (e: IllegalArgumentException) {
            Log.e("GuardianDetails", "Error decoding Base64 string: ${e.message}")
            null // Return null if decoding fails
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


}
