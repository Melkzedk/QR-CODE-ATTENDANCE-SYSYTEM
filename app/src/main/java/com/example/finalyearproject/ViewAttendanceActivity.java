package com.example.finalyearproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Date;

public class ViewAttendanceActivity extends AppCompatActivity {

    private Spinner courseSpinner;
    private ListView attendanceListView;
    private DatabaseReference dbRef;

    private ArrayList<String> courseList = new ArrayList<>();
    private ArrayAdapter<String> courseAdapter;

    private ArrayList<String> attendanceList = new ArrayList<>();
    private ArrayAdapter<String> attendanceAdapter;

    private String studentId = "";
    private String regNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        courseSpinner = findViewById(R.id.courseSpinner);
        attendanceListView = findViewById(R.id.attendanceListView);
        dbRef = FirebaseDatabase.getInstance().getReference();

        // ✅ Read session from correct preferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        studentId = prefs.getString("userId", "");

        if (studentId.isEmpty()) {
            Toast.makeText(this, "Student not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(courseAdapter);

        attendanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        attendanceListView.setAdapter(attendanceAdapter);

        // Step 1: Load student regNumber first
        dbRef.child("Users").child("Students").child(studentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        regNumber = snapshot.child("regNumber").getValue(String.class);
                        Log.d("DEBUG_REG", "regNumber = " + regNumber);

                        if (regNumber == null || regNumber.isEmpty()) {
                            Toast.makeText(ViewAttendanceActivity.this, "Reg number not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        loadCourses(); // Only load courses after regNumber is available

                        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String selectedCourse = courseList.get(position);
                                if (!selectedCourse.startsWith("⚠️")) {
                                    loadAttendanceSessions(selectedCourse);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                attendanceList.clear();
                                attendanceAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ViewAttendanceActivity.this, "Error loading student info", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCourses() {
        dbRef.child("Courses").addListenerForSingleValueEvent(new ValueEventListener() { // ✅ Capital C
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear();

                Log.d("DEBUG_COURSE", "Courses found: " + snapshot.getChildrenCount());

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String courseCode = snap.child("courseCode").getValue(String.class);
                    Log.d("DEBUG_COURSE", "Found course: " + courseCode);

                    if (courseCode != null) {
                        courseList.add(courseCode);
                    }
                }

                if (courseList.isEmpty()) {
                    courseList.add("⚠️ No courses found");
                }

                courseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG_COURSE", "Failed to load courses: " + error.getMessage());
                Toast.makeText(ViewAttendanceActivity.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAttendanceSessions(String courseCode) {
        dbRef.child("attendance_sessions")
                .orderByChild("courseCode")
                .equalTo(courseCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        attendanceList.clear();

                        for (DataSnapshot sessionSnap : snapshot.getChildren()) {
                            if (sessionSnap.hasChild("attendees") &&
                                    sessionSnap.child("attendees").hasChild(regNumber)) {

                                Long timestamp = sessionSnap.child("timestamp").getValue(Long.class);
                                String date = (timestamp != null)
                                        ? DateFormat.format("dd MMM yyyy, HH:mm", new Date(timestamp)).toString()
                                        : "Unknown Date";

                                attendanceList.add("📅 " + date + " | ✅ Present");
                            }
                        }

                        if (attendanceList.isEmpty()) {
                            attendanceList.add("❌ No attendance records found for this course.");
                        }

                        attendanceAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ViewAttendanceActivity.this, "Error loading attendance", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
