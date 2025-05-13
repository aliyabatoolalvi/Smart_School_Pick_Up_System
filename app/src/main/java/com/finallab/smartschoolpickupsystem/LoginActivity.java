package com.finallab.smartschoolpickupsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.finallab.smartschoolpickupsystem.Activities.AddGuardian;
import com.finallab.smartschoolpickupsystem.Activities.MainActivity;
import com.finallab.smartschoolpickupsystem.Guard.GuardEmailLoginActivity;
import com.finallab.smartschoolpickupsystem.Guard.GuardPhoneLoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private TextView registersign, forgotpassword;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private ProgressBar progress;
    private SharedPreferences sharedPref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_login);
        progress = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.Name);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.next);
        registersign = findViewById(R.id.registersign);
        forgotpassword = findViewById(R.id.forgot);

        sharedPref = getSharedPreferences("AdminPrefs", MODE_PRIVATE);

        TextView phoneLoginText = findViewById(R.id.phoneLoginText);
        phoneLoginText.setOnClickListener(v -> {
            startActivity(new Intent(this, GuardEmailLoginActivity.class));
        });

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Utilities.showErrorSnack(findViewById(android.R.id.content), this, "Enter email and password");
                return;
            }

            if (!Utilities.isNetworkConnected(this)) {
                Utilities.showNotConnectedSnack(findViewById(android.R.id.content), this);
                return;
            }

            progress.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        if (task.isSuccessful()) {
                            loginButton.setEnabled(true);
                            sharedPref.edit()
                                    .putString("admin_email", email)
                                    .putString("admin_password", password)
                                    .putString("admin_uid", FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .apply();
                            checkUserRoleAndRedirect();
                        }
                        if (!task.isSuccessful()) {
                            passwordEditText.setText("");
                            loginButton.setEnabled(true);

                        }

                    });
        });


        registersign.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUp.class);
            startActivity(intent);
        });

        forgotpassword.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Reset email sent. Check your inbox!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void checkUserRoleAndRedirect() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    progress.setVisibility(View.GONE);

                    if (!document.exists()) {
                        Toast.makeText(this, "User record not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String role = document.getString("role");
                    if (TextUtils.isEmpty(role)) {
                        Toast.makeText(this, "No role assigned. Contact admin.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ Save session
                    SharedPreferences.Editor adminEditor = getSharedPreferences("AdminPrefs", MODE_PRIVATE).edit();
                    adminEditor.putString("admin_userId", userId);
                    adminEditor.apply();

                    SharedPreferences userEditor = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    userEditor.edit().putString("user_role", role).apply();

                    Intent intent;

                    switch (role) {
                        case "schoolAdmin":
                            intent = new Intent(this, HomeActivity.class);
                            break;
                        case "guardian":
                            intent = new Intent(this, ParentDashboardActivity.class);
                            break;
                        case "guard":
                            intent = new Intent(this, AddGuardian.class); // or GuardDashboard
                            break;
                        default:
                            Toast.makeText(this, "Unrecognized role. Contact support.", Toast.LENGTH_SHORT).show();
                            return;
                    }

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Error fetching role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


//    private void fallbackLoginForGuard(String email, String password) {
//        FirebaseFirestore.getInstance().collection("guards")
//                .whereEqualTo("email", email)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    progress.setVisibility(View.GONE);
//                    if (!queryDocumentSnapshots.isEmpty()) {
//                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
//                        String storedPassword = doc.getString("password");
//                        String role = doc.getString("role");
//
//                        if (storedPassword != null && storedPassword.trim().equals(password.trim()) && "guard".equals(role)) {
//                            // ✅ Guard login success
//                            SharedPreferences.Editor userEditor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
//                            userEditor.putString("user_role", "guard");
//                            userEditor.putString("guard_id", doc.getId());
//                            userEditor.putString("guard_email", email);
//                            userEditor.apply();
//
//                            Intent intent = new Intent(LoginActivity.this, AddGuardian.class); // or GuardDashboard
//                            startActivity(intent);
//                            finish();
//                        } else {
//                            Toast.makeText(this, "Invalid credentials for guard", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(this, "Authentication failed: invalid credentials", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    progress.setVisibility(View.GONE);
//                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }
//    private void showGuardLoginMethodDialog() {
//        String[] options = {"Login with Phone (OTP)", "Login with Email & Password"};
//
//        new MaterialAlertDialogBuilder(this)
//                .setTitle("Select Login Method")
//                .setItems(options, (dialog, which) -> {
//                    Intent intent;
//                    if (which == 0) {
//                        intent = new Intent(this, GuardPhoneLoginActivity.class);
//                    } else {
//                        intent = new Intent(this, GuardEmailLoginActivity.class);
//                    }
//                    startActivity(intent);
//                })
//                .setBackground(getDrawable(R.drawable.dialog_bg)) // optional for rounded corners
//                .show();
//    }



}
