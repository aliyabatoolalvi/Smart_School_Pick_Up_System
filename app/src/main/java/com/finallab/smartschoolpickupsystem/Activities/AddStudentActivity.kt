package com.finallab.smartschoolpickupsystem.Activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.databinding.ActivityAddStudentBinding

class AddStudentActivity:AppCompatActivity() {
    lateinit var binding: ActivityAddStudentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.regS.setOnClickListener() {
            if
                    (binding.Sname.editText?.text.toString()
                    .isEmpty() || binding.reg.editText?.text.toString().isEmpty()
                || binding.Class.editText?.text.toString()
                    .isEmpty() || binding.section.editText?.text.toString().isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val student = Student(
                    Sname = binding.Sname.editText?.text.toString(),
                    reg = binding.reg.editText?.text.toString(),
                    Class = binding.Class.editText?.text.toString(),
                    section = binding.section.editText?.text.toString()
                )
                AppDatabase.getDatabase(this).studentDao().insert(student)
                Toast.makeText(this,"Student Registered",Toast.LENGTH_SHORT).show()
                finish()

            }

        }
    }
}