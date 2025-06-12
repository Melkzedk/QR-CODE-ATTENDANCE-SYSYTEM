package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

    Spinner subjectSpinner;
    Button scanQRBtn;
    String selectedSubject;
    TextView attendanceValue;

    ArrayList<String> courseList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    DatabaseReference coursesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        subjectSpinner = findViewById(R.id.subjectSpinner);
        scanQRBtn = findViewById(R.id.scanQRBtn);
        attendanceValue = findViewById(R.id.attendanceValue);

        // Set static attendance value for now
        attendanceValue.setText("0%");

        // Initialize adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, courseList);
        subjectSpinner.setAdapter(adapter);

        // Firebase reference
        coursesRef = FirebaseDatabase.getInstance().getReference("courses");

        // Fetch courses from Firebase
        fetchCourses();

        // Scan QR Button
        scanQRBtn.setOnClickListener(v -> {
            selectedSubject = subjectSpinner.getSelectedItem().toString();
            Intent intent = new Intent(this, QRScannerActivity.class);
            intent.putExtra("subject", selectedSubject);
            startActivity(intent);
        });
    }

    private void fetchCourses() {
        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear(); // Clear before adding fresh data
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    String courseCode = courseSnapshot.child("courseCode").getValue(String.class);
                    String courseName = courseSnapshot.child("courseName").getValue(String.class);
                    if (courseCode != null && courseName != null) {
                        courseList.add(courseCode + " - " + courseName);
                    }
                }
                adapter.notifyDataSetChanged(); // Refresh spinner
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentDashboardActivity.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
