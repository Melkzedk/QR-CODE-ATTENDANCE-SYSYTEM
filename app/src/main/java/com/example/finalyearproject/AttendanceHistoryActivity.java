package com.example.finalyearproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    private ArrayAdapter<String> adapter;
    private ArrayList<String> attendanceList = new ArrayList<>();
    private DatabaseReference attendanceRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        attendanceHistoryListView = findViewById(R.id.attendanceHistoryListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        attendanceHistoryListView.setAdapter(adapter);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance");

        fetchAttendanceHistory();
    }

    private void fetchAttendanceHistory() {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceList.clear();
                boolean found = false;

                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    String courseCode = courseSnap.getKey();

                    for (DataSnapshot timestampSnap : courseSnap.getChildren()) {
                        String timestampKey = timestampSnap.getKey();

                        if (timestampSnap.hasChild(uid)) {
                            found = true;
                            try {
                                long timestamp = Long.parseLong(timestampKey);
                                String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                        .format(new Date(timestamp));

                                attendanceList.add(courseCode + " - " + date);
                            } catch (Exception e) {
                                Log.e("AttendanceHistory", "Invalid timestamp: " + timestampKey);
                            }
                        }
                    }
                }

                if (found && !attendanceList.isEmpty()) {
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AttendanceHistoryActivity.this, "No attendance history found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AttendanceHistoryActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
