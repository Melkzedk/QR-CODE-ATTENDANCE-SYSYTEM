package com.example.finalyearproject;

import android.os.Bundle;
import android.text.format.DateFormat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        courseSpinner = findViewById(R.id.courseSpinner);
        attendanceListView = findViewById(R.id.attendanceListView);

        dbRef = FirebaseDatabase.getInstance().getReference();

        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(courseAdapter);

        attendanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        attendanceListView.setAdapter(attendanceAdapter);

        loadCourses();

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = courseList.get(position);
                loadAttendanceSessions(selectedCourse);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                attendanceList.clear();
                attendanceAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadCourses() {
        dbRef.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String courseCode = snap.child("courseCode").getValue(String.class);
                    if (courseCode != null) {
                        courseList.add(courseCode);
                    }
                }
                courseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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
                            long timestamp = sessionSnap.child("timestamp").getValue(Long.class);
                            String date = DateFormat.format("dd MMM yyyy", new Date(timestamp)).toString();

                            int count = 0;
                            if (sessionSnap.hasChild("attendees")) {
                                count = (int) sessionSnap.child("attendees").getChildrenCount();
                            }

                            attendanceList.add("📅 " + date + " | 👥 Students Present: " + count);
                        }
                        attendanceAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ViewAttendanceActivity.this, "Error loading attendance sessions", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
