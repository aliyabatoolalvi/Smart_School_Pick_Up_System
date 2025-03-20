package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finallab.smartschoolpickupsystem.Activities.AddStudentActivity
import com.finallab.smartschoolpickupsystem.Recycler.OnStudentDeletedListener
import com.finallab.smartschoolpickupsystem.Recycler.RecyclerViewAdapter
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() , OnStudentDeletedListener{
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.addS.setOnClickListener {

            startActivity(Intent(this, AddStudentActivity::class.java))
        }
        binding.backButton.setOnClickListener{
            super.onBackPressed()
        }


    }

//    override fun onResume() {
//        super.onResume()
//        val adapter= RecyclerViewAdapter(AppDatabase.getDatabase(this).studentDao().getAllStudents().toMutableList(), lifecycleScope )
//        binding.recyclerview.adapter=adapter
//        binding.recyclerview.layoutManager= LinearLayoutManager(this)
//    }

    override fun onResume() {
        super.onResume()

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val students = AppDatabase.getDatabase(this).studentDao().getStudentsByUserId(userId).toMutableList()
            val studentList: MutableList<Any> = students.toMutableList() // Cast to MutableList<Any>

            val adapter = RecyclerViewAdapter(studentList, lifecycleScope, this)

            binding.recyclerview.adapter = adapter
            binding.recyclerview.layoutManager = LinearLayoutManager(this)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onStudentDeleted() {
        onResume() // Reload student list when a student is deleted
    }

}