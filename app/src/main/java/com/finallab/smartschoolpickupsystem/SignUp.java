package com.finallab.smartschoolpickupsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, schoolNameEditText, schoolAddressEditText;
    private TextView loginLink;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.email1);
        passwordEditText = findViewById(R.id.password1);
        schoolNameEditText = findViewById(R.id.schoolName);
        schoolAddressEditText = findViewById(R.id.schoolAddress);
        signUpButton = findViewById(R.id.Signup);
        loginLink = findViewById(R.id.loginLink);
        progress = findViewById(R.id.progressSign);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String schoolName = schoolNameEditText.getText().toString().trim();
                String schoolAddress = schoolAddressEditText.getText().toString().trim();

                if (!Utilities.isNetworkConnected(SignUp.this)) {
                    Utilities.showNotConnectedSnack(findViewById(android.R.id.content), SignUp.this);
                    return;
                }

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Utilities.showErrorSnack(findViewById(android.R.id.content), SignUp.this, "Enter email or password");
                    return;
                }

                if (TextUtils.isEmpty(schoolName) || TextUtils.isEmpty(schoolAddress)) {
                    Utilities.showErrorSnack(findViewById(android.R.id.content), SignUp.this, "Enter school name or address");
                    return;
                }

                progress.setVisibility(View.VISIBLE);
                signUpButton.setEnabled(false);

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        String userId = user.getUid();
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("email", email);
                                        userData.put("schoolName", schoolName);
                                        userData.put("schoolAddress", schoolAddress);

                                        db.collection("users").document(userId)
                                                .set(userData)
                                                .addOnCompleteListener(task1 -> {
                                                    progress.setVisibility(View.GONE);
                                                    signUpButton.setEnabled(true);

                                                    if (task1.isSuccessful()) {
                                                        Toast.makeText(SignUp.this, "Sign-up successful", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(SignUp.this, LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Exception e = task1.getException();
                                                        if (e != null) {
                                                            Toast.makeText(SignUp.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                        } else {
                                                            Toast.makeText(SignUp.this, "Unknown Firestore error", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    progress.setVisibility(View.GONE);
                                    signUpButton.setEnabled(true);

                                    Exception e = task.getException();
                                    if (e != null) {
                                        String errorMessage = e.getMessage();
                                        if (errorMessage != null && errorMessage.contains("email address is already in use")) {
                                            Toast.makeText(SignUp.this, "This email is already registered. Please log in.", Toast.LENGTH_LONG).show();
                                        } else if (errorMessage != null && errorMessage.contains("password should be at least")) {
                                            Toast.makeText(SignUp.this, "Weak password! Choose a stronger password.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(SignUp.this, "Authentication failed: " + errorMessage, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            }
                        });
            }
        });

        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
