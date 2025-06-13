package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LecturerDashboardActivity extends AppCompatActivity {

    Button addCourseBtn, manageCourseBtn, markAttendanceBtn, viewAttendanceBtn, generateReportBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_dashboard);

        // Link buttons
        addCourseBtn = findViewById(R.id.addCourseBtn);
        manageCourseBtn = findViewById(R.id.manageCourseBtn);
        markAttendanceBtn = findViewById(R.id.markAttendanceBtn);
        viewAttendanceBtn = findViewById(R.id.viewAttendanceBtn);
        generateReportBtn = findViewById(R.id.generateReportBtn);

        // Button actions
        addCourseBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AddCourseActivity.class))
        );

        manageCourseBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ManageCoursesActivity.class))  // This will be your Manage Courses screen
        );

        markAttendanceBtn.setOnClickListener(v ->
                startActivity(new Intent(this, GenerateQRActivity.class))
        );

        viewAttendanceBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ViewAttendanceActivity.class))  // This will show attendance records
        );

        generateReportBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AttendanceReportActivity.class))
        );
    }
}
