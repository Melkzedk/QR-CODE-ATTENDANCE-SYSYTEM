package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    Button btnAddStudent, btnAddLecturer, btnViewStudents, btnViewLecturers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnAddStudent = findViewById(R.id.btnAddStudent);
        btnAddLecturer = findViewById(R.id.btnAddLecturer);
        btnViewStudents = findViewById(R.id.btnViewStudents);
        btnViewLecturers = findViewById(R.id.btnViewLecturers);

        btnAddStudent.setOnClickListener(v ->
                startActivity(new Intent(this, AddStudentActivity.class)));

        btnAddLecturer.setOnClickListener(v ->
                startActivity(new Intent(this, AddLecturerActivity.class)));

        btnViewStudents.setOnClickListener(v ->
                startActivity(new Intent(this, ViewStudentsActivity.class)));

        btnViewLecturers.setOnClickListener(v ->
                startActivity(new Intent(this, ViewLecturersActivity.class)));
    }
}
