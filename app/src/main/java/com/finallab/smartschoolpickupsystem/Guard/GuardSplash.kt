package com.finallab.smartschoolpickupsystem.Guard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.finallab.smartschoolpickupsystem.R

class GuardSplash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guard_splash)
//        Handler(mainLooper).postDelayed({
//            startActivity(Intent(this, GuardListActivity::class.java))
//            finish()
//        }, 3000)
    }
}