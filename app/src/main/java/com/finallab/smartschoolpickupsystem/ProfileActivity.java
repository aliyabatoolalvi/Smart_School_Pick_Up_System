package com.finallab.smartschoolpickupsystem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.SharedPreferences;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.finallab.smartschoolpickupsystem.Room.AppDatabase;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {
    private ImageView profilePicture,backArrow;
    private TextView name, email, cnic, phone;
    private Button backButton;
    private FirebaseFirestore db;
    private AppDatabase localDb;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profilePicture = findViewById(R.id.profile1);
        name = findViewById(R.id.name2);
        email = findViewById(R.id.email2);
        cnic = findViewById(R.id.cnic2);
        phone = findViewById(R.id.phone2);
         backArrow = findViewById(R.id.back_arrow);



        db = FirebaseFirestore.getInstance();
        localDb = AppDatabase.getDatabase(getApplicationContext());


        loadUserProfile();

        backArrow.setOnClickListener(v -> finish());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String guardianUID = currentUser.getUid();

        db.collection("guardians").document(guardianUID)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String gName = document.getString("Gname");
                        String gEmail = document.getString("Email");
                        String gCNIC = document.getString("CNIC");
                        String gNumber = document.getString("number");
                        String profilePicUrl = document.getString("profile_picture_url");

                        name.setText(gName);
                        email.setText(gEmail);
                        cnic.setText("CNIC: " + gCNIC);
                        phone.setText("Phone: " + gNumber);

                        if (profilePicUrl != null && !profilePicUrl.trim().isEmpty()) {
                            Glide.with(ProfileActivity.this)
                                    .load(profilePicUrl.trim())
                                    .placeholder(R.drawable.guardian)
                                    .error(R.drawable.guardian)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(profilePicture);
                        } else {
                            profilePicture.setImageResource(R.drawable.guardian);
                        }

                        // ✅ Save to Room
                        GuardianEntity entity = new GuardianEntity();
                        entity.guardianId = guardianUID;
                        entity.Gname = gName;
                        entity.Email = gEmail;
                        entity.CNIC = gCNIC;
                        entity.number = gNumber;
                        entity.profile_picture_url = profilePicUrl;

                        new Thread(() -> localDb.guardianEntityDao().insertGuardian(entity)).start();

                    } else {
                        Toast.makeText(this, "Guardian profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // ❌ Offline fallback
                    new Thread(() -> {
                        GuardianEntity guardian = localDb.guardianEntityDao().getGuardianById(guardianUID);
                        runOnUiThread(() -> {
                            if (guardian != null) {
                                name.setText(guardian.Gname);
                                email.setText(guardian.Email);
                                cnic.setText("CNIC: " + guardian.CNIC);
                                phone.setText("Phone: " + guardian.number);

                                if (guardian.profile_picture_url != null && !guardian.profile_picture_url.trim().isEmpty()) {
                                    Glide.with(ProfileActivity.this)
                                            .load(guardian.profile_picture_url.trim())
                                            .placeholder(R.drawable.guardian)
                                            .error(R.drawable.guardian)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .into(profilePicture);
                                } else {
                                    profilePicture.setImageResource(R.drawable.guardian);
                                }

                                Toast.makeText(ProfileActivity.this, "Loaded offline profile", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Offline profile not found", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                });
    }
}