package com.example.finalyearproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class AttendanceHistoryActivity extends AppCompatActivity {

    private ListView attendanceHistoryListView;
    private Spinner courseFilterSpinner;
    private ArrayAdapter<String> attendanceAdapter, courseAdapter;
    private ArrayList<String> attendanceList = new ArrayList<>();
    private ArrayList<String> courseList = new ArrayList<>();

    private DatabaseReference databaseRef;
    private String uid;
    private String regNumber; // Used to match in attendance_sessions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        attendanceHistoryListView = findViewById(R.id.attendanceHistoryListView);
        courseFilterSpinner = findViewById(R.id.courseFilterSpinner);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        uid = prefs.getString("userId", null); // This is the Firebase UID for the student

        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseRef = FirebaseDatabase.getInstance().getReference();

        attendanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        attendanceHistoryListView.setAdapter(attendanceAdapter);
        courseFilterSpinner.setAdapter(courseAdapter);

        // Step 1: Fetch regNumber using uid
        databaseRef.child("Users").child("Students").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            regNumber = snapshot.child("regNumber").getValue(String.class);
                            if (regNumber != null) {
                                fetchCourses(); // Step 2: Proceed to fetch courses after getting regNumber
                            } else {
                                Toast.makeText(AttendanceHistoryActivity.this, "RegNumber not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AttendanceHistoryActivity.this, "Student record not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AttendanceHistoryActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        courseFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = courseList.get(position);
                if (!selectedCourse.equals("Select Course")) {
                    fetchAttendanceForCourse(selectedCourse);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchCourses() {
        databaseRef.child("attendance_sessions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> uniqueCourses = new HashSet<>();
                for (DataSnapshot sessionSnap : snapshot.getChildren()) {
                    if (sessionSnap.child("attendees").hasChild(regNumber)) {
                        String courseCode = sessionSnap.child("courseCode").getValue(String.class);
                        if (courseCode != null && !courseCode.isEmpty()) {
                            uniqueCourses.add(courseCode);
                        }
                    }
                }

                courseList.clear();
                courseList.add("Select Course");
                courseList.addAll(uniqueCourses);
                courseAdapter.notifyDataSetChanged();

                if (courseList.size() > 1) {
                    courseFilterSpinner.setSelection(1);
                    fetchAttendanceForCourse(courseList.get(1));
                } else {
                    Toast.makeText(AttendanceHistoryActivity.this, "No attendance records found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AttendanceHistoryActivity.this, "Failed to fetch courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAttendanceForCourse(String courseCode) {
        databaseRef.child("attendance_sessions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceList.clear();
                boolean found = false;

                for (DataSnapshot sessionSnap : snapshot.getChildren()) {
                    String sessionCourse = sessionSnap.child("courseCode").getValue(String.class);
                    if (sessionCourse != null && sessionCourse.equals(courseCode)) {
                        if (sessionSnap.child("attendees").hasChild(regNumber)) {
                            found = true;
                            long timestamp = sessionSnap.child("timestamp").getValue(Long.class);
                            String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    .format(new Date(timestamp));
                            attendanceList.add(courseCode + " - " + date);
                        }
                    }
                }

                if (!found) {
                    attendanceList.add("No attendance records found for " + courseCode);
                }

                attendanceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AttendanceHistoryActivity.this, "Failed to fetch attendance", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
