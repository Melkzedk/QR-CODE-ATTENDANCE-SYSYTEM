package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

public class StudentDashboardActivity extends AppCompatActivity {

    AutoCompleteTextView subjectDropdown;
    TextView attendanceValue;
    ArrayList<String> courseList = new ArrayList<>();
    ArrayAdapter<String> adapter;
    DatabaseReference coursesRef;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    String selectedSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        subjectDropdown = findViewById(R.id.subjectDropdown);
        attendanceValue = findViewById(R.id.attendanceValue);
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

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, courseList);
        subjectDropdown.setAdapter(adapter);

        coursesRef = FirebaseDatabase.getInstance().getReference("courses");
        fetchCourses();

        // Handle navigation drawer item clicks
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_scan_qr) {
                    selectedSubject = subjectDropdown.getText().toString().trim();
                    if (selectedSubject.isEmpty()) {
                        Toast.makeText(this, "Please select a subject", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(this, QRScannerActivity.class);
                        intent.putExtra("subject", selectedSubject);
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
                    startActivity(new Intent(this, TimetableActivity.class));
                } else if (itemId == R.id.nav_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }

                if (drawerLayout != null) {
                    drawerLayout.closeDrawers();
                }

                return true;
            });

            // Setup profile in header
            View headerView = navigationView.getHeaderView(0);
            TextView profileName = headerView.findViewById(R.id.profileName);
            TextView profileEmail = headerView.findViewById(R.id.profileEmail);
            ImageView profileImage = headerView.findViewById(R.id.profileImage);

            profileEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            profileName.setText("Student Name"); // Optionally fetch from DB

            profileImage.setOnClickListener(v ->
                    Toast.makeText(this, "Image picker not yet implemented", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void fetchCourses() {
        if (coursesRef != null) {
            coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    courseList.clear();
                    for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                        String courseCode = courseSnapshot.child("courseCode").getValue(String.class);
                        String courseName = courseSnapshot.child("courseName").getValue(String.class);
                        if (courseCode != null && courseName != null) {
                            courseList.add(courseCode + " - " + courseName);
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
}
