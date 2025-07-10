//Generate QR Code Activity
package com.example.finalyearproject;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.database.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import android.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GenerateQRActivity extends AppCompatActivity {
    private Spinner courseSpinner;
    private Button generateQRButton;
    private ImageView qrImageView;

    private DatabaseReference dbRef;
    private ArrayList<String> courseList = new ArrayList<>();
    private Map<String, String> courseCodeMap = new HashMap<>();

    private static final int LOCATION_PERMISSION_REQUEST = 102;
    private FusedLocationProviderClient fusedLocationClient;
    private String lecturerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userType = prefs.getString("userType", null);
        lecturerId = prefs.getString("userId", null);

        if (userType == null || lecturerId == null || !userType.equals("lecturer")) {
            Toast.makeText(this, "Unauthorized. Please login.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_generate_qr);

        courseSpinner = findViewById(R.id.courseSpinner);
        generateQRButton = findViewById(R.id.generateQRButton);
        qrImageView = findViewById(R.id.qrImageView);

        dbRef = FirebaseDatabase.getInstance().getReference("courses");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        generateQRButton.setEnabled(false);
        loadCourses();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }

        generateQRButton.setOnClickListener(v -> generateQRCode());
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

                if (!courseList.isEmpty()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, courseList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    courseSpinner.setAdapter(adapter);
                    generateQRButton.setEnabled(true);
                } else {
                    Toast.makeText(this, "No courses found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateQRCode() {
        if (courseSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedDisplayName = courseSpinner.getSelectedItem().toString();
        String courseCode = courseCodeMap.get(selectedDisplayName);
        long timestamp = System.currentTimeMillis();

        if (courseCode == null) {
            Toast.makeText(this, "Course code is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(this, "Unable to get location.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference("attendance_sessions");
                    String sessionId = sessionRef.push().getKey();

                    if (sessionId == null) {
                        Toast.makeText(this, "Failed to generate session ID", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("courseCode", courseCode);
                    data.put("timestamp", timestamp);
                    data.put("lecturerId", lecturerId);
                    data.put("latitude", location.getLatitude());
                    data.put("longitude", location.getLongitude());
                    data.put("attendees", new HashMap<>()); // Ensure attendees is initialized

                    sessionRef.child(sessionId).setValue(data)
                            .addOnSuccessListener(aVoid -> {
                                try {
                                    String qrData = sessionId + "|" + timestamp;
                                    BarcodeEncoder encoder = new BarcodeEncoder();
                                    Bitmap bitmap = encoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400);
                                    qrImageView.setImageBitmap(bitmap);
                                    Toast.makeText(this, "✅ QR generated successfully!", Toast.LENGTH_SHORT).show();
                                } catch (WriterException e) {
                                    Toast.makeText(this, "QR Generation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "❌ Failed to save QR data", Toast.LENGTH_SHORT).show());

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Location fetch failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}