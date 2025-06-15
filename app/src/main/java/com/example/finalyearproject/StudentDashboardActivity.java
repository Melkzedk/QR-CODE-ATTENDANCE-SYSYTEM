package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentDashboardActivity extends AppCompatActivity {

    AutoCompleteTextView subjectDropdown;
    TextView attendanceValue;
    Button btnSubmitSubject;
    ArrayList<String> courseDisplayList = new ArrayList<>();
    Map<String, String> displayToCodeMap = new HashMap<>();
    ArrayAdapter<String> adapter;
    DatabaseReference coursesRef, attendanceRef;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        subjectDropdown = findViewById(R.id.subjectDropdown);
        attendanceValue = findViewById(R.id.attendanceValue);
        btnSubmitSubject = findViewById(R.id.btnSubmitSubject);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if (drawerLayout != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }

        attendanceValue.setText("0%");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, courseDisplayList);
        subjectDropdown.setAdapter(adapter);

        coursesRef = FirebaseDatabase.getInstance().getReference("courses");
        attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance");

        fetchCourses();

        btnSubmitSubject.setOnClickListener(v -> {
            String selectedDisplay = subjectDropdown.getText().toString().trim();
            if (!displayToCodeMap.containsKey(selectedDisplay)) {
                Toast.makeText(this, "Please select a valid subject", Toast.LENGTH_SHORT).show();
                return;
            }
            String courseCode = displayToCodeMap.get(selectedDisplay);
            fetchAttendancePercentage(courseCode);
        });

        setupNavigation();
    }

    private void fetchCourses() {
        if (coursesRef != null) {
            coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    courseDisplayList.clear();
                    displayToCodeMap.clear();

                    for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                        String courseCode = courseSnapshot.child("courseCode").getValue(String.class);
                        String courseName = courseSnapshot.child("courseName").getValue(String.class);

                        if (courseCode != null && courseName != null) {
                            String display = courseCode + " - " + courseName;
                            courseDisplayList.add(display);
                            displayToCodeMap.put(display, courseCode);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(StudentDashboardActivity.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void fetchAttendancePercentage(String courseCode) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        attendanceRef.child(courseCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long attended = 0;
                long totalSessions = snapshot.getChildrenCount();

                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                    if (sessionSnapshot.hasChild(uid)) {
                        Boolean isPresent = sessionSnapshot.child(uid).getValue(Boolean.class);
                        if (Boolean.TRUE.equals(isPresent)) {
                            attended++;
                        }
                    }
                }

                if (totalSessions == 0) {
                    attendanceValue.setText("0%");
                } else {
                    long percent = (attended * 100) / totalSessions;
                    attendanceValue.setText(percent + "%");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentDashboardActivity.this, "Failed to fetch attendance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_scan_qr) {
                    String selected = subjectDropdown.getText().toString().trim();
                    if (selected.isEmpty()) {
                        Toast.makeText(this, "Please select a subject", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(this, QRScannerActivity.class);
                        intent.putExtra("subject", selected);
                        startActivity(intent);
                    }
                } else if (itemId == R.id.nav_attendance_history) {
                    startActivity(new Intent(this, AttendanceHistoryActivity.class));
                } else if (itemId == R.id.nav_announcements) {
                    startActivity(new Intent(this, AnnouncementsActivity.class));
                } else if (itemId == R.id.nav_download_report) {
                    startActivity(new Intent(this, DownloadReportActivity.class));
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileSettingsActivity.class));
                } else if (itemId == R.id.nav_contact) {
                    startActivity(new Intent(this, ContactLecturerActivity.class));
                } else if (itemId == R.id.nav_timetable) {
                    startActivity(new Intent(this, ViewTimetableActivity.class)); // ✅ Updated to student version
                } else if (itemId == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }

                drawerLayout.closeDrawers();
                return true;
            });

            View headerView = navigationView.getHeaderView(0);
            TextView profileName = headerView.findViewById(R.id.profileName);
            TextView profileEmail = headerView.findViewById(R.id.profileEmail);
            ImageView profileImage = headerView.findViewById(R.id.profileImage);

            profileEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            profileName.setText("Student Name");

            profileImage.setOnClickListener(v ->
                    Toast.makeText(this, "Image picker not yet implemented", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
