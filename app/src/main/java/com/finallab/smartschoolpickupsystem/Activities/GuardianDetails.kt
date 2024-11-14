package com.finallab.smartschoolpickupsystem.Activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.DataModels.Student
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

        binding.textView5.text = guardian.Gname
        binding.textView7.text = "Phone: "+guardian.number
        binding.textView8.text = "CNIC: " + guardian.CNIC
        binding.textView9.text = "Email: " + guardian.Email
    }
}