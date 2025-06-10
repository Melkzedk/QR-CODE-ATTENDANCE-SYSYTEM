package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class StudentDashboardActivity extends AppCompatActivity {
    Spinner subjectSpinner;
    Button scanQRBtn;
    String selectedSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        subjectSpinner = findViewById(R.id.subjectSpinner);
        scanQRBtn = findViewById(R.id.scanQRBtn);

        String[] subjects = {"Mathematics", "Science", "English"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjects);
        subjectSpinner.setAdapter(adapter);

        scanQRBtn.setOnClickListener(v -> {
            selectedSubject = subjectSpinner.getSelectedItem().toString();
            Intent intent = new Intent(this, QRScannerActivity.class);
            intent.putExtra("subject", selectedSubject);
            startActivity(intent);
        });
    }
}
