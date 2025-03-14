package com.finallab.smartschoolpickupsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String schoolName = schoolNameEditText.getText().toString().trim();
                String schoolAddress = schoolAddressEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(SignUp.this, "Enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(schoolName) || TextUtils.isEmpty(schoolAddress)) {
                    Toast.makeText(SignUp.this, "Enter school name and address", Toast.LENGTH_SHORT).show();
                    return;
                }

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


//                                        db.collection("users").document(userId)
//                                                .set(userData)
//                                                .addOnCompleteListener(task1 -> {
//                                                    if (task1.isSuccessful()) {
//                                                        Toast.makeText(SignUp.this, "Sign-up successful", Toast.LENGTH_SHORT).show();
//                                                        Intent intent = new Intent(SignUp.this, LoginActivity.class);
//                                                        startActivity(intent);
//                                                        finish();
//                                                    } else {
//                                                        Toast.makeText(SignUp.this, "Error saving user data: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                                    }
//                                                });
                                        db.collection("users").document(userId)
                                                .set(userData)
                                                .addOnCompleteListener(task1 -> {
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
                                    Toast.makeText(SignUp.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        loginLink.setOnClickListener(v -> {
            // Navigate to LoginActivity
            Intent intent = new Intent(SignUp.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
