package com.finallab.smartschoolpickupsystem;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
// for qr code
//        implementation("com.google.zxing:core:3.4.1")
//        implementation("androidmads.library.qrgenearator:QRGenearator:1.0.3")

    }
}