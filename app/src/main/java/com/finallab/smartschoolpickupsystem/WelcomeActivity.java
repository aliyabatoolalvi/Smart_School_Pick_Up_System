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

        mAuth = FirebaseAuth.getInstance();

        forward = findViewById(R.id.advance);

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
            intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        } else {
            intent = new Intent(WelcomeActivity.this, HomeActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
