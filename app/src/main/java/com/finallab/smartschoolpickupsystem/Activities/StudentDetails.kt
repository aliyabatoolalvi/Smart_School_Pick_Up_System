package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finallab.smartschoolpickupsystem.Activities.AddGuardian
import com.finallab.smartschoolpickupsystem.Recycler.RecyclerViewAdapter
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.DataModels.Student

import com.finallab.smartschoolpickupsystem.databinding.ActivityStudentDetailsBinding

class StudentDetails : AppCompatActivity() {
    lateinit var binding: ActivityStudentDetailsBinding
    lateinit  var student: Student
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityStudentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("id", -1)
        student = AppDatabase.getDatabase(this).studentDao().getstudentById(id)

        binding.nameS.text = "Name: " + student.Sname
        binding.rollno.text = "Reg no: " + student.reg
        binding.ClassS.text = "Class: " + student.studentClass
        binding.sectionS.text = "Section: " + student.section

        binding.addG.setOnClickListener {
            startActivity(Intent(this, AddGuardian::class.java)
                .putExtra("id", id)
                .putExtra("studentDocumentID", student.studentDocId)) // This is the studentDocId
        }

        binding.backButton.setOnClickListener{
            super.onBackPressed()
        }

    }
    override fun onResume() {
        super.onResume()
        val adapter= RecyclerViewAdapter(AppDatabase.getDatabase(this).guardianDao().getAllguardians(student.id).toMutableList(), lifecycleScope,null)
        binding.recyclerView.adapter=adapter
        binding.recyclerView.layoutManager= LinearLayoutManager(this)
    }

}
