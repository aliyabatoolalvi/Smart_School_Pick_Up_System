package com.finallab.smartschoolpickupsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.finallab.smartschoolpickupsystem.Room.AppDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.time.Instant;

public class QrCodeActivity extends AppCompatActivity {
    private ImageView qrCodeImage;
    private ImageView backButton;
    private ProgressBar qrProgressBar;
    private FirebaseFirestore db;
    private AppDatabase localDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        qrCodeImage = findViewById(R.id.qrCodeImageView);
        backButton = findViewById(R.id.back_arrow);
        qrProgressBar = findViewById(R.id.qrProgressBar);

        db = FirebaseFirestore.getInstance();
        localDb = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "guardian_local_db").build();

        fetchQRCode();

        backButton.setOnClickListener(v -> finish());
    }

    private void fetchQRCode() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String guardianUID = currentUser.getUid();
        qrProgressBar.setVisibility(View.VISIBLE);

        db.collection("guardians").document(guardianUID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    qrProgressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        String qrData = documentSnapshot.getString("QRcodeData");
                        if (qrData != null && !qrData.isEmpty()) {
                            generateQRCode(qrData);


                            GuardianEntity entity = new GuardianEntity();
                            entity.guardianId = guardianUID;
                            entity.QRcodeData = qrData;

                            new Thread(() -> localDb.guardianEntityDao().insertGuardian(entity)).start();
                        } else {
                            Toast.makeText(this, "QR Code not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Guardian document not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    qrProgressBar.setVisibility(View.GONE);
                    Log.e("Firestore", "Error fetching QR Code", e);
                    Toast.makeText(this, "Offline mode: trying local QR", Toast.LENGTH_SHORT).show();


                    new Thread(() -> {
                        GuardianEntity guardian = localDb.guardianEntityDao().getGuardianById(guardianUID);
                        if (guardian != null && guardian.QRcodeData != null) {
                            runOnUiThread(() -> generateQRCode(guardian.QRcodeData));
                        } else {
                            runOnUiThread(() -> Toast.makeText(QrCodeActivity.this, "No offline QR found", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                });
    }

    private void generateQRCode(String qrData) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);
            qrCodeImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e("QR Code", "Error generating QR Code", e);
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }
}