package com.finallab.smartschoolpickupsystem;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

//    private TextView textViewName, textViewEmail;
//    private FirebaseAuth mAuth;
//    private FirebaseFirestore db;
//
//    public ProfileFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_profile, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        textViewName = view.findViewById(R.id.textViewName);
//        textViewEmail = view.findViewById(R.id.textViewEmail);
//
//        // Initialize Firebase instances
//        mAuth = FirebaseAuth.getInstance();
//        db = FirebaseFirestore.getInstance();
//
//        // Fetch and display user data
//        fetchUserData();
//    }
//
//    private void fetchUserData() {
//        String userId = mAuth.getCurrentUser().getUid();
//
//        // Retrieve user data from Firestore
//        db.collection("users").document(userId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        // Map data to User class
//                        String name = documentSnapshot.getString("name");
//                        String email = documentSnapshot.getString("email");
//
//                        // Set data to TextViews
//                        textViewName.setText("Name: " + name);
//                        textViewEmail.setText("Email: " + email);
//                    } else {
//                        Toast.makeText(getContext(), "No user data found", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    // Handle error
//                    Toast.makeText(getContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//                }
    }

