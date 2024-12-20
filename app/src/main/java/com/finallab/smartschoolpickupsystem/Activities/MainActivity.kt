package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finallab.smartschoolpickupsystem.Activities.AddStudentActivity
import com.finallab.smartschoolpickupsystem.Recycler.RecyclerViewAdapter
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
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

    override fun onResume() {
        super.onResume()
        val adapter= RecyclerViewAdapter(AppDatabase.getDatabase(this).studentDao().getAllStudents().toMutableList(), lifecycleScope )
        binding.recyclerview.adapter=adapter
        binding.recyclerview.layoutManager= LinearLayoutManager(this)
    }
}