package com.finallab.smartschoolpickupsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.finallab.smartschoolpickupsystem.Activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
    ImageView forward;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize the ImageView after setting the content view
        forward = findViewById(R.id.advance);

        forward.setOnClickListener(v -> {
            // Navigate to the new activity
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User not logged in, redirect to Login/Signup screen
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            // User already logged in, redirect to Home screen
            Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        finish(); // Close the splash screen
    }
}

//
//Handler(mainLooper).postDelayed({
//    startActivity(Intent(this, MainActivity::class.java))
//    finish()
//}, 1000)
