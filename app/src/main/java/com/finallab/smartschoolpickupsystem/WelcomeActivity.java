package com.finallab.smartschoolpickupsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    private ImageButton forward;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Find the button by its ID
        forward = findViewById(R.id.advance);

        // Set a click listener for the button
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUserAndNavigate();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserAndNavigate();
    }

    private void checkUserAndNavigate() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Intent intent;

        if (currentUser == null) {
            // If no user is logged in, go to LoginActivity
            intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        } else {
            // If a user is logged in, go to HomeActivity
            intent = new Intent(WelcomeActivity.this, HomeActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
