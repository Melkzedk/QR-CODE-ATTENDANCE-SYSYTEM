package com.example.finalyearproject;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GenerateQRActivity extends AppCompatActivity {

    private Spinner courseSpinner;
    private Button generateQRButton;
    private ImageView qrImageView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ArrayList<String> courseList = new ArrayList<>();
    private Map<String, String> courseCodeMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);

        courseSpinner = findViewById(R.id.courseSpinner);
        generateQRButton = findViewById(R.id.generateQRButton);
        qrImageView = findViewById(R.id.qrImageView);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        generateQRButton.setEnabled(false); // Disable button until data loads

        loadCourses();

        generateQRButton.setOnClickListener(v -> {
            if (courseSpinner.getSelectedItem() == null) {
                Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedCourse = courseSpinner.getSelectedItem().toString();
            String courseCode = courseCodeMap.get(selectedCourse);

            if (courseCode == null) {
                Toast.makeText(this, "Invalid course selected", Toast.LENGTH_SHORT).show();
                return;
            }

            long timestamp = System.currentTimeMillis();
            String qrData = courseCode + "|" + timestamp;

            try {
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400);
                qrImageView.setImageBitmap(bitmap);

                Map<String, Object> data = new HashMap<>();
                data.put("courseCode", courseCode);
                data.put("timestamp", timestamp);
                data.put("lecturerId", mAuth.getCurrentUser().getUid());

                db.collection("attendance_sessions").add(data)
                        .addOnSuccessListener(docRef ->
                                Toast.makeText(this, "QR data saved", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to save QR data", Toast.LENGTH_SHORT).show());

            } catch (WriterException e) {
                Toast.makeText(this, "QR Generation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCourses() {
        String lecturerId = mAuth.getCurrentUser().getUid();

        db.collection("courses")
                .whereEqualTo("lecturerId", lecturerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        String name = doc.getString("courseName");
                        String code = doc.getString("courseCode");

                        if (name != null && code != null) {
                            courseList.add(name);
                            courseCodeMap.put(name, code);
                        }
                    }

                    if (courseList.isEmpty()) {
                        Toast.makeText(this, "No courses found for you", Toast.LENGTH_SHORT).show();
                    } else {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, courseList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        courseSpinner.setAdapter(adapter);
                        generateQRButton.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
