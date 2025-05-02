package com.finallab.smartschoolpickupsystem;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.finallab.smartschoolpickupsystem.Activities.StudentDetails;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment implements OnStudentDeletedListener, OnDataChangedListener {


    private TextView textViewName, textViewEmail, textViewAddress, studentcount, guardiancount;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button logoutButton = view.findViewById(R.id.logout);
        MaterialButton report = view.findViewById(R.id.report);
        textViewName = view.findViewById(R.id.nameschool);
        textViewEmail = view.findViewById(R.id.emailschool);
        textViewAddress = view.findViewById(R.id.addressschool);  // Added TextView for address
        studentcount = view.findViewById(R.id.studenttotal);  // TextView for student count
        guardiancount = view.findViewById(R.id.totalguardians);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        logoutButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(getActivity())
                    .setTitle("Logout?")
                    .setMessage("Logging out will end your session.")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        // ✅ Clear AdminPrefs
                        SharedPreferences sharedPref = requireActivity().getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
                        sharedPref.edit().clear().apply();

                        // ✅ Clear stored user role
                        SharedPreferences userPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        userPref.edit().clear().apply();

                        // ✅ Sign out from Firebase
                        mAuth.signOut();
                        Toast.makeText(getContext(), "You’ll need to login again next time.", Toast.LENGTH_SHORT).show();

                        // ✅ Redirect to LoginActivity
                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
                        startActivity(intent);
                        requireActivity().finish();

                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        });


        fetchUserData();
        fetchStudentAndGuardianCounts();

    }

    private void fetchUserData() {
        if (mAuth.getCurrentUser() == null) {
            showToast("User not logged in");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("schoolName");
                        String email = documentSnapshot.getString("schoolEmail");
                        String address = documentSnapshot.getString("schoolAddress");  // Fetching the address

                        textViewName.setText(name);
                        textViewEmail.setText(email);
                        textViewAddress.setText(address);  // Displaying the address
                    } else {
                        showToast("No user data found");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Error fetching data: " + e.getMessage());
                });
    }
    private void fetchStudentAndGuardianCounts() {
        if (mAuth.getCurrentUser() == null) {
            showToast("User not authenticated");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("students")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int studentCount = querySnapshot.size();
                    studentcount.setText("Total Students: " +studentCount);
                })
                .addOnFailureListener(e -> {
                    showToast("Error fetching student count: " + e.getMessage());
                });

        db.collection("guardians")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int guardianCount = querySnapshot.size();
                    guardiancount.setText("Total Guardians: "+ guardianCount);
                })
                .addOnFailureListener(e -> {
                    showToast("Error fetching guardian count: " + e.getMessage());
                });
    }


    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDataUpdated() {
        fetchStudentAndGuardianCounts();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StudentDetails) {
            ((StudentDetails) context).setOnItemDeletedListener(this::onDataUpdated);
        }
    }

}
