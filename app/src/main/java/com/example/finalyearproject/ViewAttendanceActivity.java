package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class ViewAttendanceActivity extends AppCompatActivity {

    Spinner courseSpinner;
    ListView attendanceListView;
    DatabaseReference dbRef;
    ArrayList<String> courseList = new ArrayList<>();
    ArrayAdapter<String> courseAdapter;
    ArrayList<String> attendanceList = new ArrayList<>();
    ArrayAdapter<String> attendanceAdapter;

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
                loadAttendanceForCourse(selectedCourse);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                attendanceList.clear();
                attendanceAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadCourses() {
        dbRef.child("Courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    courseList.add(snap.getKey());
                }
                courseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewAttendanceActivity.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAttendanceForCourse(String courseName) {
        dbRef.child("Attendance").child(courseName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String studentId = snap.getKey();
                    long presentCount = snap.getChildrenCount();
                    attendanceList.add(studentId + " - Sessions Present: " + presentCount);
                }
                attendanceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewAttendanceActivity.this, "Failed to load attendance", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
