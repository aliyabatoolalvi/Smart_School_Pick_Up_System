package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityGuardianDetailsBinding
import com.finallab.smartschoolpickupsystem.databinding.ActivityStudentDetailsBinding

class GuardianDetails : AppCompatActivity() {
    lateinit var binding: ActivityGuardianDetailsBinding
    lateinit  var guardian: Guardian
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityGuardianDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("id", -1)
        guardian = AppDatabase.getDatabase(this).guardianDao().getguardianById(id)

        binding.textView5.text = "Name: \n"+guardian.Gname
        binding.textView7.text = "Phone: \n"+guardian.number
        binding.textView8.text = "CNIC: \n" + guardian.CNIC
        binding.textView9.text = "Email: \n" + guardian.Email

        val phoneTextView: TextView = binding.textView7
        val emailTextView: TextView = binding.textView9

        phoneTextView.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("${guardian.number}")
            startActivity(intent)
        }

        emailTextView.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("${guardian.Email}")
            startActivity(intent)
        }
        phoneTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
        emailTextView.setTextColor(ContextCompat.getColor(this, R.color.black))

    }
}