package com.finallab.smartschoolpickupsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.finallab.smartschoolpickupsystem.Activities.MainActivity;

public class WelcomeActivity extends AppCompatActivity {
    ImageView forward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize the ImageView after setting the content view
        forward = findViewById(R.id.advance);

        forward.setOnClickListener(v -> {
            // Navigate to the new activity
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}

//
//Handler(mainLooper).postDelayed({
//    startActivity(Intent(this, MainActivity::class.java))
//    finish()
//}, 1000)
