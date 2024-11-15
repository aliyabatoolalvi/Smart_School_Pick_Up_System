package com.finallab.smartschoolpickupsystem.Activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityAddGuardianBinding

class AddGuardian : AppCompatActivity() {
    lateinit var binding: ActivityAddGuardianBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGuardianBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val id = intent.getIntExtra("id", -1)
        binding.regG.setOnClickListener() {
            if (binding.Gname.editText?.text.toString()
                    .isEmpty() || binding.number.editText?.text.toString().isEmpty()
                || binding.CNIC.editText?.text.toString()
                    .isEmpty() || binding.Email.editText?.text.toString().isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                if (binding.CNIC.editText?.text.toString().length == 13 && binding.CNIC.editText?.text.toString()
                        .all { it.isDigit() } && binding.number.editText?.text.toString().length == 11 && binding.number.editText?.text.toString()
                        .all { it.isDigit() }
                ) {
                    val student = Guardian(
                        studentID = id,
                        Gname = binding.Gname.editText?.text.toString(),
                        number = binding.number.editText?.text.toString(),
                        CNIC = binding.CNIC.editText?.text.toString(),
                        Email = binding.Email.editText?.text.toString()
                        QRcodeData =
                    )
                    AppDatabase.getDatabase(this).guardianDao().insert(student)
                    Toast.makeText(this, "Guardian Registered", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    if (binding.CNIC.editText?.text.toString().length != 13 || !binding.CNIC.editText?.text.toString()
                            .all { it.isDigit() }
                    ) {
                        binding.CNIC.error = "CNIC must be 13 digits only"
                    } else {
                        binding.number.error = "Invalid phone number"
                    }


                }
            }
        }
    }
}