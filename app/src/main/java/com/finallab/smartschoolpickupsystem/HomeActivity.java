package com.finallab.smartschoolpickupsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db= FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer);
        NavigationView navigationView = findViewById(R.id.navview);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView schoonaTextView = headerView.findViewById(R.id.schoolname);
        schoonaTextView.setText("New School Name");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open_nav,
                R.string.close_nav
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.home);
        }

        fetchSchoolName();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.home) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        } else if (itemId == R.id.settings) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SettingFragment())
                    .commit();
        } else if (itemId == R.id.profile) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .commit();
        } else if (itemId == R.id.privacy) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new PrivacyFragment())
                    .commit();
        } else if (itemId == R.id.menulogout) {
            SharedPreferences sharedPref = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);

            sharedPref.edit().clear().apply();

            // ✅ Clear stored user role
            SharedPreferences userPref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            userPref.edit().clear().apply();
            Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void fetchSchoolName() {
        if (mAuth.getCurrentUser() == null) {
            showToast("User not logged in");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String schoolName = documentSnapshot.getString("schoolName");

                        NavigationView navigationView = findViewById(R.id.navview);

                        View headerView = navigationView.getHeaderView(0);
                        TextView schoonaTextView = headerView.findViewById(R.id.schoolname);
                        schoonaTextView.setText(schoolName);

                    } else {
                        NavigationView navigationView = findViewById(R.id.navview);

                        View headerView = navigationView.getHeaderView(0);
                        TextView schoonaTextView = headerView.findViewById(R.id.schoolname);
                        schoonaTextView.setText("School Name");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Error fetching data: " + e.getMessage());
                });
    }
    private void showToast(String message) {
        if (this != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
