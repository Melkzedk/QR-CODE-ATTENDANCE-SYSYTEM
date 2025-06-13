package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StudentDashboardActivity extends AppCompatActivity {

    AutoCompleteTextView subjectDropdown;
    Button scanQRBtn, viewAttendanceHistoryBtn, viewAnnouncementsBtn,
            downloadReportBtn, profileSettingsBtn, contactLecturerBtn;
    String selectedSubject;
    TextView attendanceValue;

    ArrayList<String> courseList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    DatabaseReference coursesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        subjectDropdown = findViewById(R.id.subjectDropdown);  // Use the new ID
        scanQRBtn = findViewById(R.id.scanQRBtn);
        attendanceValue = findViewById(R.id.attendanceValue);

        viewAttendanceHistoryBtn = findViewById(R.id.viewAttendanceHistoryBtn);
        viewAnnouncementsBtn = findViewById(R.id.viewAnnouncementsBtn);
        downloadReportBtn = findViewById(R.id.downloadReportBtn);
        profileSettingsBtn = findViewById(R.id.profileSettingsBtn);
        contactLecturerBtn = findViewById(R.id.contactLecturerBtn);

        // Set static attendance value for now
        attendanceValue.setText("0%");

        // Initialize adapter for AutoCompleteTextView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, courseList);
        subjectDropdown.setAdapter(adapter);

        // Firebase reference
        coursesRef = FirebaseDatabase.getInstance().getReference("courses");

        // Fetch courses
        fetchCourses();

        // Scan QR
        scanQRBtn.setOnClickListener(v -> {
            selectedSubject = subjectDropdown.getText().toString();
            if (selectedSubject.isEmpty()) {
                Toast.makeText(this, "Please select a subject", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, QRScannerActivity.class);
                intent.putExtra("subject", selectedSubject);
                startActivity(intent);
            }
        });

        // View attendance history
        viewAttendanceHistoryBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AttendanceHistoryActivity.class))
        );

        // View announcements
        viewAnnouncementsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AnnouncementsActivity.class))
        );

        // Download report
        downloadReportBtn.setOnClickListener(v ->
                startActivity(new Intent(this, DownloadReportActivity.class))
        );

        // Profile / settings
        profileSettingsBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileSettingsActivity.class))
        );

        // Contact lecturer
        contactLecturerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ContactLecturerActivity.class))
        );
    }

    private void fetchCourses() {
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
