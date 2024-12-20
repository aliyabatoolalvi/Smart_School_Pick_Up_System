package com.finallab.smartschoolpickupsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.finallab.smartschoolpickupsystem.Activities.AddStudentActivity;
import com.finallab.smartschoolpickupsystem.Activities.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager;
    private Button listStudentsButton, addStudentButton;
    private TextView navigationText;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = view.findViewById(R.id.viewPager);
        listStudentsButton = view.findViewById(R.id.listStudentsButton);
        addStudentButton = view.findViewById(R.id.addStudentButton);
        navigationText = view.findViewById(R.id.navigationText);

        navigationText.setText("Welcome! Use the buttons below to manage students or enjoy the carousel.");

        setupCarousel();

        listStudentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        });

        addStudentButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddStudentActivity.class);
            startActivity(intent);
        });

    }

    private void setupCarousel() {
        List<Integer> images = new ArrayList<>();
        images.add(R.drawable.rushhourschool); // Replace with your actual drawable resource IDs
        images.add(R.drawable.titleimage);
        images.add(R.drawable.lineongate);
        images.add(R.drawable.qrbanner);
        images.add(R.drawable.rushhourschool2);


        CarouselAdaptor adapter = new CarouselAdaptor(images);
        viewPager.setAdapter(adapter);
    }
}
