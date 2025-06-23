package com.example.finalyearproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Date;

public class ViewAttendanceActivity extends AppCompatActivity {

    private ListView attendanceListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> attendanceData;
    private DatabaseReference dbRef;
    private String lecturerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        attendanceListView = findViewById(R.id.attendanceListView);
        attendanceData = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceData);
        attendanceListView.setAdapter(adapter);

        dbRef = FirebaseDatabase.getInstance().getReference();

        // 🔑 Get lecturer ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        lecturerId = prefs.getString("userId", null);

        if (lecturerId == null) {
            Toast.makeText(this, "Lecturer not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        loadLecturerSessions();
    }

    private void loadLecturerSessions() {
        dbRef.child("attendance_sessions")
                .orderByChild("lecturerId")
                .equalTo(lecturerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        attendanceData.clear();

                        for (DataSnapshot sessionSnap : snapshot.getChildren()) {
                            String courseCode = sessionSnap.child("courseCode").getValue(String.class);
                            Long timestamp = sessionSnap.child("timestamp").getValue(Long.class);
                            String date = (timestamp != null) ?
                                    DateFormat.format("dd MMM yyyy", new Date(timestamp)).toString() :
                                    "Unknown Date";

                            DataSnapshot attendeesSnap = sessionSnap.child("attendees");
                            if (!attendeesSnap.exists()) continue;

                            StringBuilder sessionText = new StringBuilder();
                            sessionText.append("📅 ").append(date)
                                    .append("\n📘 Course: ").append(courseCode)
                                    .append("\n👥 Students:");

                            for (DataSnapshot regSnap : attendeesSnap.getChildren()) {
                                sessionText.append("\n   - ").append(regSnap.getKey());
                            }

                            attendanceData.add(sessionText.toString());
                        }

                        if (attendanceData.isEmpty()) {
                            attendanceData.add("No attendance records found.");
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ViewAttendanceActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                        Log.e("FIREBASE_ERROR", error.getMessage());
                    }
                });
    }
}
