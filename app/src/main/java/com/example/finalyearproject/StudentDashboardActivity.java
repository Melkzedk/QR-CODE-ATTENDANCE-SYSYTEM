package com.example.finalyearproject;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentDashboardActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    AutoCompleteTextView subjectDropdown;
    TextView attendanceValue;
    Button btnSubmitSubject;
    ArrayList<String> courseDisplayList = new ArrayList<>();
    Map<String, String> displayToCodeMap = new HashMap<>();
    ArrayAdapter<String> adapter;
    DatabaseReference coursesRef, attendanceRef, usersRef;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    String regNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Corrected session handling
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userType = prefs.getString("userType", null);
        regNumber = prefs.getString("userId", null);

        if (userType == null || !userType.equals("student") || regNumber == null || regNumber.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_student_dashboard);

        // 🔔 Create Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "class_channel", "Class Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 🔐 Request POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }

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
        usersRef = FirebaseDatabase.getInstance().getReference("Users/Students");

        fetchCourses();
        loadProfileHeader();

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

    private void fetchAttendancePercentage(String courseCode) {
        attendanceRef.child(courseCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long attended = 0;
                long totalSessions = snapshot.getChildrenCount();

                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                    Boolean isPresent = sessionSnapshot.child(regNumber).getValue(Boolean.class);
                    if (Boolean.TRUE.equals(isPresent)) {
                        attended++;
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

    private void loadProfileHeader() {
        usersRef.orderByChild("regNumber").equalTo(regNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot student : snapshot.getChildren()) {
                                String name = student.child("name").getValue(String.class);
                                String email = student.child("email").getValue(String.class);

                                View headerView = navigationView.getHeaderView(0);
                                TextView profileName = headerView.findViewById(R.id.profileName);
                                TextView profileEmail = headerView.findViewById(R.id.profileEmail);
                                ImageView profileImage = headerView.findViewById(R.id.profileImage);

                                profileName.setText(name != null ? name : "Student");
                                profileEmail.setText(email != null ? email : "");

                                profileImage.setOnClickListener(v -> {
                                    Toast.makeText(StudentDashboardActivity.this, "Image picker not yet implemented", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StudentDashboardActivity.this, "Error loading profile info", Toast.LENGTH_SHORT).show();
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
                        intent.putExtra("regNumber", regNumber);
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
                    startActivity(new Intent(this, ViewTimetableActivity.class));
                } else if (itemId == R.id.nav_submit_assignment) {
                    startActivity(new Intent(this, SubmitAssignmentActivity.class));
                } else if (itemId == R.id.nav_logout) {
                    // ✅ Clear saved session and logout
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    prefs.edit().clear().apply();

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                drawerLayout.closeDrawers();
                return true;
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications will be disabled unless permission is granted", Toast.LENGTH_LONG).show();
            }
        }
    }
}
