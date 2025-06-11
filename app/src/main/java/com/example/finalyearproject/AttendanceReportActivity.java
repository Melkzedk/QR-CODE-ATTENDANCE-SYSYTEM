package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

public class AttendanceReportActivity extends AppCompatActivity {

    private Spinner courseSpinner;
    private Button fetchReportButton;
    private ListView reportListView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ArrayList<String> courseList = new ArrayList<>();
    private Map<String, String> courseCodeMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);

        courseSpinner = findViewById(R.id.courseSpinner);
        fetchReportButton = findViewById(R.id.fetchReportButton);
        reportListView = findViewById(R.id.reportListView);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadCourses();

        fetchReportButton.setOnClickListener(v -> {
            String selectedCourse = courseSpinner.getSelectedItem().toString();
            String courseCode = courseCodeMap.get(selectedCourse);
            if (courseCode == null) {
                Toast.makeText(this, "No course selected", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchAttendanceReport(courseCode);
        });
    }

    private void loadCourses() {
        String lecturerId = mAuth.getCurrentUser().getUid();

        db.collection("courses")
                .whereEqualTo("lecturerId", lecturerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("courseName");
                        String code = doc.getString("courseCode");

                        if (name != null && code != null) {
                            courseList.add(name);
                            courseCodeMap.put(name, code);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    courseSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load courses", Toast.LENGTH_SHORT).show());
    }

    private void fetchAttendanceReport(String courseCode) {
        db.collection("attendance_sessions")
                .whereEqualTo("courseCode", courseCode)
                .get()
                .addOnSuccessListener(sessionSnapshots -> {
                    ArrayList<String> reportLines = new ArrayList<>();

                    for (QueryDocumentSnapshot sessionDoc : sessionSnapshots) {
                        long timestamp = sessionDoc.getLong("timestamp");
                        String sessionId = sessionDoc.getId();

                        String dateStr = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date(timestamp));
                        reportLines.add("📅 Session on " + dateStr);

                        db.collection("attendance_records")
                                .whereEqualTo("sessionId", sessionId)
                                .get()
                                .addOnSuccessListener(records -> {
                                    if (records.isEmpty()) {
                                        reportLines.add("  ❌ No attendance records.");
                                    } else {
                                        for (QueryDocumentSnapshot record : records) {
                                            String studentName = record.getString("studentName");
                                            reportLines.add("  ✅ " + studentName);
                                        }
                                    }

                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reportLines);
                                    reportListView.setAdapter(adapter);
                                })
                                .addOnFailureListener(e -> reportLines.add("Error fetching records."));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching sessions", Toast.LENGTH_SHORT).show());
    }
}
