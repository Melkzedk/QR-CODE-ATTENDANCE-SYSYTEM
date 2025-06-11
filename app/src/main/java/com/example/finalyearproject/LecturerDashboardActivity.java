package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LecturerDashboardActivity extends AppCompatActivity {

    Button addCourseBtn, markAttendanceBtn, generateReportBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_dashboard);

        addCourseBtn = findViewById(R.id.addCourseBtn);
        markAttendanceBtn = findViewById(R.id.markAttendanceBtn);
        generateReportBtn = findViewById(R.id.generateReportBtn);

        addCourseBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AddCourseActivity.class))
        );

        markAttendanceBtn.setOnClickListener(v ->
                startActivity(new Intent(this, GenerateQRActivity.class)) // this activity will generate QR
        );

        generateReportBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AttendanceReportActivity.class)) // to show report
        );
    }
}
