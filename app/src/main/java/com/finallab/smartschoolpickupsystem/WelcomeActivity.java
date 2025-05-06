package com.finallab.smartschoolpickupsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.finallab.smartschoolpickupsystem.Guard.ScannerActivity;
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

        if (currentUser == null) {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        String uid = currentUser.getUid();

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getString("role") != null) {
                        String role = documentSnapshot.getString("role");

                        Intent intent;
                        switch (role) {
                            case "admin":
                                intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                                break;
                            case "guardian":
                                intent = new Intent(WelcomeActivity.this, ParentDashboardActivity.class);
                                break;
                            default:
                                // fallback to checking guards collection if role is unknown
                                checkIfGuard(uid);
                                return;
                        }

                        startActivity(intent);
                        finish();
                    } else {
                        // Fallback: maybe it's a guard
                        checkIfGuard(uid);
                    }
                })
                .addOnFailureListener(e -> {
                    // If failed to check user collection, fallback to guard
                    checkIfGuard(uid);
                });
    }

    private void checkIfGuard(String uid) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("guards")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        startActivity(new Intent(WelcomeActivity.this, ScannerActivity.class));
                    } else {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                    finish();
                });
    }

}
