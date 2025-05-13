package com.finallab.smartschoolpickupsystem;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.finallab.smartschoolpickupsystem.Activities.AddGuardian;
import com.finallab.smartschoolpickupsystem.Activities.AddStudentActivity;
import com.finallab.smartschoolpickupsystem.Activities.AdminFeedbackActivity;
import com.finallab.smartschoolpickupsystem.Activities.AdminReport;
import com.finallab.smartschoolpickupsystem.Activities.MainActivity;
import com.finallab.smartschoolpickupsystem.Guard.GuardAddActivity;
import com.finallab.smartschoolpickupsystem.Guard.GuardListActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private Button listStudentsButton, manageGuardian, showUserID, manageGuards;
    private TextView navigationText;

    private Handler carouselHandler = new Handler();
    private Runnable carouselRunnable;
    private int currentIndex = 0;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        MaterialButton manageStudentsBtn = view.findViewById(R.id.manageStudentsBtn);
        MaterialButton manageGuardiansBtn = view.findViewById(R.id.manageGuardiansBtn);
        MaterialButton reportsBtn = view.findViewById(R.id.reportbtn);
        MaterialButton feedbackBtn = view.findViewById(R.id.feedbackBtn);
        MaterialButton schoolProfileBtn = view.findViewById(R.id.schoolprofile);

        viewPager = view.findViewById(R.id.viewPager);
        listStudentsButton = view.findViewById(R.id.manageStudentsBtn);
        manageGuardian = view.findViewById(R.id.manageGuardiansBtn);
        showUserID = view.findViewById(R.id.schoolprofile);
        manageGuards= view.findViewById(R.id.manageGuardsBtn);

//        navigationText.setText("Welcome! Use the buttons below to manage students or enjoy the carousel.");

        setupCarousel();

        listStudentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        manageGuards.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GuardListActivity.class);
            startActivity(intent);
        });
        reportsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AdminReport.class);
            startActivity(intent);
        });

        manageGuardian.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddGuardian.class);
            startActivity(intent);
        });

        feedbackBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AdminFeedbackActivity.class);
            startActivity(intent);
        });


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final String userId = currentUser != null ? currentUser.getUid() : null; // Effectively final

        // Handle showUserID click
        showUserID.setOnClickListener(v -> {
            Fragment profileFragment = new ProfileFragment();
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack(null)
                    .commit();

        });
    }
    private void setupCarousel() {
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.rushhourschool);
        images.add(R.drawable.titleimage);
        images.add(R.drawable.lineongate);
        images.add(R.drawable.qrbanner);
        images.add(R.drawable.rushhourschool2);

        CarouselAdaptor adapter = new CarouselAdaptor(images);
        viewPager.setAdapter(adapter);

        // Carousel visual settings
        viewPager.setOffscreenPageLimit(3);
        viewPager.setOverScrollMode(ViewPager2.OVER_SCROLL_NEVER);

        // 💫 Smooth incline-style animation
        viewPager.setPageTransformer((page, position) -> {
            page.setTranslationX(-position * page.getWidth() * 0.3f);  // subtle slide
            page.setRotationY(position * 15);                          // tilt effect
            page.setAlpha(1 - Math.abs(position));                     // fade edges
        });

        // Auto-scroll every 3 seconds
        carouselRunnable = () -> {
            if (adapter.getItemCount() > 0) {
                currentIndex = (currentIndex + 1) % adapter.getItemCount();
                carouselHandler.postDelayed(carouselRunnable, 8000);
            }
        };

        carouselHandler.postDelayed(carouselRunnable, 8000);
    }

    @Override
    public void onPause() {
        super.onPause();
        carouselHandler.removeCallbacks(carouselRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (carouselRunnable != null) {
            carouselHandler.postDelayed(carouselRunnable, 5000);
        }
    }

}
