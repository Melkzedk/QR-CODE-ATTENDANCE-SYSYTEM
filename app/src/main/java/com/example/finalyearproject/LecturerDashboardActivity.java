//Lecturer Dashboard Activity

package com.example.finalyearproject;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LecturerDashboardActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    private static final String CHANNEL_ID = "class_channel";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_dashboard);

        setupToolbarAndDrawer();
        setupNotificationChannel();
        requestNotificationPermissionIfNeeded();
        setupNavigationListener();
        setupQuickActionCards();
        loadLecturerName();
    }

    private void setupToolbarAndDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupQuickActionCards() {
        CardView cardMarkAttendance = findViewById(R.id.cardMarkAttendance);
        CardView cardSendMessage = findViewById(R.id.cardSendMessage);
        CardView cardGenerateReport = findViewById(R.id.cardGenerateReport);

        cardMarkAttendance.setOnClickListener(v -> openActivity(GenerateQRActivity.class));
        cardSendMessage.setOnClickListener(v -> openActivity(SendMessageActivity.class));
        cardGenerateReport.setOnClickListener(v -> openActivity(AttendanceReportActivity.class));
    }

    private void loadLecturerName() {
        TextView lecturerNameTextView = findViewById(R.id.lecturerName);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("lecturers").child(uid).child("name");

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.getValue(String.class);
                    if (name != null) {
                        lecturerNameTextView.setText("Welcome, " + name);
                    } else {
                        lecturerNameTextView.setText("Welcome, Lecturer");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    lecturerNameTextView.setText("Welcome, Lecturer");
                    Toast.makeText(LecturerDashboardActivity.this, "Failed to load name", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Class Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }

    private void setupNavigationListener() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_add_course) {
                openActivity(AddCourseActivity.class);
            } else if (id == R.id.nav_manage_course) {
                openActivity(ManageCoursesActivity.class);
            } else if (id == R.id.nav_view_timetable) {
                openActivity(ViewTimetableActivity.class);
            } else if (id == R.id.nav_mark_attendance) {
                openActivity(GenerateQRActivity.class);
            } else if (id == R.id.nav_view_attendance) {
                openActivity(ViewAttendanceActivity.class);
            } else if (id == R.id.nav_generate_report) {
                openActivity(AttendanceReportActivity.class);
            } else if (id == R.id.nav_send_message) {
                openActivity(SendMessageActivity.class);
            } else if (id == R.id.nav_view_messages) {
                openActivity(AnnouncementsActivity.class);
            } else if (id == R.id.nav_view_assignments) {
                openActivity(ViewAssignmentsActivity.class);  // ✅ New item added here
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                openActivity(LoginActivity.class);
                finish();
            } else {
                Toast.makeText(this, "Unknown action", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void openActivity(Class<?> targetActivity) {
        startActivity(new Intent(this, targetActivity));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications may not be shown without permission", Toast.LENGTH_LONG).show();
            }
        }
    }
}
