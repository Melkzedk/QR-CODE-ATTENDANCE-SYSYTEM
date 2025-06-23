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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class ViewAttendanceActivity extends AppCompatActivity {

    private ListView attendanceListView;
    private Spinner courseFilterSpinner;
    private Button exportBtn;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> attendanceData;
    private ArrayList<String> courseCodes = new ArrayList<>();
    private ArrayAdapter<String> courseAdapter;

    private DatabaseReference dbRef;
    private String lecturerId;
    private Map<String, String> regToNameMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        attendanceListView = findViewById(R.id.attendanceListView);
        courseFilterSpinner = findViewById(R.id.courseFilterSpinner);
        exportBtn = findViewById(R.id.exportBtn);

        attendanceData = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceData);
        attendanceListView.setAdapter(adapter);

        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseCodes);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseFilterSpinner.setAdapter(courseAdapter);

        dbRef = FirebaseDatabase.getInstance().getReference();

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        lecturerId = prefs.getString("userId", null);

        if (lecturerId == null) {
            Toast.makeText(this, "Lecturer not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load students, courses, and sessions
        loadStudentNames(() -> {
            loadCourses();
            courseFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedCourse = courseCodes.get(position);
                    loadLecturerSessions(selectedCourse);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });

        // Export to Excel when button is clicked
        exportBtn.setOnClickListener(v -> exportToExcel());
    }

    private void loadStudentNames(Runnable callback) {
        dbRef.child("Users").child("Students")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot studentSnap : snapshot.getChildren()) {
                            String reg = studentSnap.child("regNumber").getValue(String.class);
                            String name = studentSnap.child("name").getValue(String.class);
                            if (reg != null && name != null) {
                                regToNameMap.put(reg, name);
                            }
                        }
                        callback.run();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ViewAttendanceActivity.this, "Failed to load student names", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCourses() {
        dbRef.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseCodes.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String code = snap.child("courseCode").getValue(String.class);
                    if (code != null) courseCodes.add(code);
                }
                courseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewAttendanceActivity.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLecturerSessions(String courseFilter) {
        dbRef.child("attendance_sessions")
                .orderByChild("lecturerId")
                .equalTo(lecturerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        attendanceData.clear();

                        for (DataSnapshot sessionSnap : snapshot.getChildren()) {
                            String courseCode = sessionSnap.child("courseCode").getValue(String.class);
                            if (courseCode == null || !courseCode.equals(courseFilter)) continue;

                            Long timestamp = sessionSnap.child("timestamp").getValue(Long.class);
                            String date = (timestamp != null)
                                    ? DateFormat.format("dd MMM yyyy", new Date(timestamp)).toString()
                                    : "Unknown Date";

                            DataSnapshot attendeesSnap = sessionSnap.child("attendees");
                            if (!attendeesSnap.exists()) continue;

                            StringBuilder sessionText = new StringBuilder();
                            sessionText.append("📅 ").append(date)
                                    .append("\n📘 Course: ").append(courseCode)
                                    .append("\n👥 Students:");

                            for (DataSnapshot regSnap : attendeesSnap.getChildren()) {
                                String reg = regSnap.getKey();
                                String name = regToNameMap.getOrDefault(reg, "Unknown Student");
                                sessionText.append("\n   - ").append(name).append(" (").append(reg).append(")");
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
                        Toast.makeText(ViewAttendanceActivity.this, "Failed to load attendance", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void exportToExcel() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Attendance Report");

            int rowIndex = 0;
            for (String entry : attendanceData) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(entry);
            }

            File file = new File(getExternalFilesDir(null), "attendance_report.xlsx");
            FileOutputStream out = new FileOutputStream(file);
            workbook.write(out);
            out.close();
            workbook.close();

            Toast.makeText(this, "Exported to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("EXPORT_ERROR", e.getMessage());
        }
    }
}
