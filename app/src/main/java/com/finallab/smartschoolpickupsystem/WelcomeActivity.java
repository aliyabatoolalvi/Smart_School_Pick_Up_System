package com.finallab.smartschoolpickupsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeActivity extends AppCompatActivity {

    private ImageButton forward;
    private FirebaseAuth mAuth;
    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mAuth = FirebaseAuth.getInstance();
        forward = findViewById(R.id.advance);

//         logo = findViewById(R.id.logoImage);
//        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
//        logo.startAnimation(fadeIn);

//        TextView tapHint = findViewById(R.id.tapHint);
//
//        tapHint.animate()
//                .alpha(1f)
//                .setDuration(800)
//                .setStartDelay(1000)
//                .start();

        // 👆 Only check when user taps forward
        forward.setOnClickListener(v -> handleSessionCheck());
    }

    private void handleSessionCheck() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            checkUserAndNavigate();
        } else {
            SharedPreferences flags = getSharedPreferences("Flags", MODE_PRIVATE);
            boolean shouldRetryLogin = flags.getBoolean("retry_admin_login", false);

            if (shouldRetryLogin) {
                flags.edit().putBoolean("retry_admin_login", false).apply();

                SharedPreferences adminPrefs = getSharedPreferences("AdminPrefs", MODE_PRIVATE);
                String email = adminPrefs.getString("admin_email", null);
                String password = adminPrefs.getString("admin_password", null);

                if (email != null && password != null) {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> checkUserAndNavigate())
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Auto-login failed. Please login manually.", Toast.LENGTH_SHORT).show();
                                navigateToLogin();
                            });
                    return;
                }
            }

            navigateToLogin();
        }
    }

    private void checkUserAndNavigate() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }

        String uid = currentUser.getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getString("role") != null) {
                        String role = documentSnapshot.getString("role");
                        Intent intent;

                        switch (role) {
                            case "admin":
                            case "schoolAdmin":
                                intent = new Intent(this, HomeActivity.class);
                                break;
                            case "guardian":
                                intent = new Intent(this, ParentDashboardActivity.class);
                                break;
                            default:
                                Toast.makeText(this, "Invalid role. Please login again.", Toast.LENGTH_SHORT).show();
                                navigateToLogin();
                                return;
                        }

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(this, "No role found. Please login again.", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
