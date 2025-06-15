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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private DatabaseReference dbRef;

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
        dbRef = FirebaseDatabase.getInstance().getReference("courses");

        generateQRButton.setEnabled(false);

        loadCourses();

        generateQRButton.setOnClickListener(v -> {
            if (courseSpinner.getSelectedItem() == null) {
                Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedDisplayName = courseSpinner.getSelectedItem().toString();
            String courseCode = courseCodeMap.get(selectedDisplayName);

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

                // Save session (optional - you can adjust this to Firestore if needed)
                DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference("attendance_sessions");
                String sessionId = sessionRef.push().getKey();

                Map<String, Object> data = new HashMap<>();
                data.put("courseCode", courseCode);
                data.put("timestamp", timestamp);
                data.put("lecturerId", mAuth.getCurrentUser().getUid());

                if (sessionId != null) {
                    sessionRef.child(sessionId).setValue(data)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this, "QR data saved", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to save QR data", Toast.LENGTH_SHORT).show());
                }

            } catch (WriterException e) {
                Toast.makeText(this, "QR Generation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCourses() {
        dbRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();

                courseList.clear();
                courseCodeMap.clear();

                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    String name = courseSnap.child("courseName").getValue(String.class);
                    String code = courseSnap.child("courseCode").getValue(String.class);

                    if (name != null && code != null) {
                        String displayName = code + " - " + name;
                        courseList.add(displayName);
                        courseCodeMap.put(displayName, code);
                    }
                }

                if (courseList.isEmpty()) {
                    Toast.makeText(this, "No courses found", Toast.LENGTH_SHORT).show();
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, courseList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    courseSpinner.setAdapter(adapter);
                    generateQRButton.setEnabled(true);
                }
            } else {
                Toast.makeText(this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
