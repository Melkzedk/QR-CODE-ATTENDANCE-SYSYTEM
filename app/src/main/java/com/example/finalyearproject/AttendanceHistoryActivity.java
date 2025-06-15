package com.example.finalyearproject;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AttendanceHistoryActivity extends AppCompatActivity {

    private ListView attendanceHistoryListView;
    private Spinner courseFilterSpinner;
    private ArrayAdapter<String> attendanceAdapter, courseAdapter;
    private ArrayList<String> attendanceList = new ArrayList<>();
    private ArrayList<String> courseList = new ArrayList<>();
    private DatabaseReference attendanceRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        attendanceHistoryListView = findViewById(R.id.attendanceHistoryListView);
        courseFilterSpinner = findViewById(R.id.courseFilterSpinner);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance");

        // Setup adapters
        attendanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        attendanceHistoryListView.setAdapter(attendanceAdapter);
        courseFilterSpinner.setAdapter(courseAdapter);

        fetchCourseList();

        courseFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = courseList.get(position);
                fetchAttendanceForCourse(selectedCourse);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void fetchCourseList() {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear();
                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    courseList.add(courseSnap.getKey());
                }
                courseAdapter.notifyDataSetChanged();
                if (!courseList.isEmpty()) {
                    fetchAttendanceForCourse(courseList.get(0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AttendanceHistoryActivity.this, "Failed to fetch courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAttendanceForCourse(String courseCode) {
        attendanceRef.child(courseCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceList.clear();
                boolean found = false;

                for (DataSnapshot timestampSnap : snapshot.getChildren()) {
                    if (timestampSnap.hasChild(uid)) {
                        found = true;
                        try {
                            long timestamp = Long.parseLong(timestampSnap.getKey());
                            String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    .format(new Date(timestamp));
                            attendanceList.add(courseCode + " - " + date);
                        } catch (Exception e) {
                            attendanceList.add(courseCode + " - Invalid date");
                        }
                    }
                }

                attendanceAdapter.notifyDataSetChanged();

                if (!found) {
                    attendanceList.add("No attendance records found for " + courseCode);
                    attendanceAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AttendanceHistoryActivity.this, "Failed to fetch attendance", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
