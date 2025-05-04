package com.finallab.smartschoolpickupsystem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.finallab.smartschoolpickupsystem.Room.AppDatabase;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
public class ParentDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private TextView Gname, number;
    private RecyclerView studentList;
    private FirebaseFirestore db;
    private List<Student> students;
    private StudentAdapter studentAdapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CardView qrCodeItem, reportItem, feedbackItem;
    private TextView navGuardianName;
    private AppDatabase localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);


        db = FirebaseFirestore.getInstance();
        localDb = com.finallab.smartschoolpickupsystem.Room.AppDatabase.getDatabase(getApplicationContext());


        // Views
        Gname = findViewById(R.id.Gname);
        number = findViewById(R.id.number);
        studentList = findViewById(R.id.studentList);
        qrCodeItem = findViewById(R.id.qrCodeItem);
        reportItem = findViewById(R.id.reportItem);
        feedbackItem = findViewById(R.id.feedbackItem);

        // Drawer & Navigation
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        navGuardianName = headerView.findViewById(R.id.nav_guardian_name);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Recycler setup
        students = new ArrayList<>();
        studentAdapter = new StudentAdapter(students, this);
        studentList.setLayoutManager(new LinearLayoutManager(this));
        studentList.setAdapter(studentAdapter);

        fetchGuardianDetails();

        // Card listeners
        qrCodeItem.setOnClickListener(v -> startActivity(new Intent(this, QrCodeActivity.class)));
        reportItem.setOnClickListener(v -> startActivity(new Intent(this, ReportActivity.class)));
        feedbackItem.setOnClickListener(v -> startActivity(new Intent(this, Feedbackactivity.class)));
    }

    private void fetchGuardianDetails() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        db.collection("guardians").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("Gname");
                        String phone = doc.getString("number");

                        Gname.setText(name != null ? name : "Unknown");
                        number.setText(phone != null ? phone : "N/A");
                        navGuardianName.setText(name != null ? name : "Guardian");

                        List<String> studentDocIds = (List<String>) doc.get("students");
                        if (studentDocIds != null && !studentDocIds.isEmpty()) {
                            fetchStudentsByDocIds(studentDocIds);
                        } else {
                            Toast.makeText(this, "No students linked to this guardian", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Guardian record not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchStudentsByDocIds(List<String> studentDocIds) {
        students.clear();

        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String docId : studentDocIds) {
            tasks.add(db.collection("students").document(docId).get());
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    List<StudentEntity> studentEntities = new ArrayList<>();

                    for (Object obj : results) {
                        DocumentSnapshot doc = (DocumentSnapshot) obj;
                        if (doc.exists()) {
                            Student student = doc.toObject(Student.class);
                            if (student != null) {
                                student.setStudentDocId(doc.getId());
                                students.add(student);

                                // Prepare Room entity
                                StudentEntity entity = new StudentEntity();
                                entity.studentId = student.getStudentDocId();
                                entity.Sname = student.getSname();
                                entity.reg = student.getReg();
                                entity.studentClass = student.getStudentClass();
                                entity.section = student.getSection();
                                entity.image = student.getImage();
                                studentEntities.add(entity);
                            }
                        }
                    }

                    studentAdapter.notifyDataSetChanged();

                    // Save to Room
                    new Thread(() -> localDb.studentEntityDao().insertStudents(studentEntities)).start();
                })
                .addOnFailureListener(e -> {
                    new Thread(() -> {
                        List<StudentEntity> cached = localDb.studentEntityDao().getAllStudents();
                        List<Student> fallbackList = new ArrayList<>();
                        for (StudentEntity s : cached) {
                            Student student = new Student();
                            student.setStudentDocId(s.studentId);
                            student.setSname(s.Sname);
                            student.setReg(s.reg);
                            student.setStudentClass(s.studentClass);
                            student.setSection(s.section);
                            student.setImage(s.image);
                            fallbackList.add(student);
                        }

                        runOnUiThread(() -> {
                            students.clear();
                            students.addAll(fallbackList);
                            studentAdapter.notifyDataSetChanged();
                            Toast.makeText(this, "Showing offline data", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                });
    }



    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_logout) {
            logoutUser();
        }

        drawerLayout.closeDrawers();
        return true;
    }
}


