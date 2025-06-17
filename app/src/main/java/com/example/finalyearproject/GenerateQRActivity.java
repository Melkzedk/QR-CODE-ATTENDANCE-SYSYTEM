package com.example.finalyearproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
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

    private static final int LOCATION_PERMISSION_REQUEST = 102;

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
        String qrData = courseCode + "|" + timestamp;

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                try {
                    BarcodeEncoder encoder = new BarcodeEncoder();
                    Bitmap bitmap = encoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400);
                    qrImageView.setImageBitmap(bitmap);

                    DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference("attendance_sessions");
                    String sessionId = sessionRef.push().getKey();

                    Map<String, Object> data = new HashMap<>();
                    data.put("courseCode", courseCode);
                    data.put("timestamp", timestamp);
                    data.put("lecturerId", mAuth.getCurrentUser().getUid());
                    data.put("latitude", location.getLatitude());
                    data.put("longitude", location.getLongitude());

                    if (sessionId != null) {
                        sessionRef.child(sessionId).setValue(data)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "QR data saved", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save QR data", Toast.LENGTH_SHORT).show());
                    }

                } catch (WriterException e) {
                    Toast.makeText(this, "QR Generation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Unable to get location. Try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show();
        }
    }
}
