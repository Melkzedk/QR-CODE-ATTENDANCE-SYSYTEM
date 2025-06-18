package com.example.finalyearproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QRScannerActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 101;
    private static final int LOCATION_PERMISSION_REQUEST = 102;

    private DecoratedBarcodeView barcodeScannerView;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        barcodeScannerView = findViewById(R.id.barcode_scanner);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Attendance");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            startScanner();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }
    }

    private void startScanner() {
        barcodeScannerView.resume();
        barcodeScannerView.decodeSingle(callback);
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            String qrData = result.getText();
            String uid = mAuth.getCurrentUser().getUid();

            String[] parts = qrData.split("\\|");
            if (parts.length != 2) {
                Toast.makeText(QRScannerActivity.this, "Invalid QR format", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String courseCode = parts[0];
            long qrTimestamp = Long.parseLong(parts[1]);

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(QRScannerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Location studentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (studentLocation != null) {
                    FirebaseDatabase.getInstance().getReference("attendance_sessions")
                            .get().addOnSuccessListener(snapshot -> {
                                boolean valid = false;
                                for (DataSnapshot s : snapshot.getChildren()) {
                                    String dbCourseCode = s.child("courseCode").getValue(String.class);
                                    Long dbTimestamp = s.child("timestamp").getValue(Long.class);
                                    Double lat = s.child("latitude").getValue(Double.class);
                                    Double lng = s.child("longitude").getValue(Double.class);

                                    if (dbCourseCode != null && dbTimestamp != null && lat != null && lng != null &&
                                            dbCourseCode.equals(courseCode) &&
                                            Math.abs(dbTimestamp - qrTimestamp) <= 60000) {
                                        Location lecturerLocation = new Location("");
                                        lecturerLocation.setLatitude(lat);
                                        lecturerLocation.setLongitude(lng);

                                        float distance = studentLocation.distanceTo(lecturerLocation);

                                        if (distance <= 50) {
                                            valid = true;

                                            // Fetch user details from "Users"
                                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
                                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot userSnap) {
                                                    if (userSnap.exists()) {
                                                        String name = userSnap.child("name").getValue(String.class);
                                                        String regNumber = userSnap.child("regNumber").getValue(String.class);

                                                        StudentAttendance attendance = new StudentAttendance(name, regNumber, System.currentTimeMillis());

                                                        DatabaseReference attendancePath = dbRef.child(courseCode)
                                                                .child(String.valueOf(qrTimestamp)).child(uid);

                                                        attendancePath.setValue(attendance)
                                                                .addOnSuccessListener(aVoid ->
                                                                        Toast.makeText(QRScannerActivity.this, "Attendance marked!", Toast.LENGTH_SHORT).show())
                                                                .addOnFailureListener(e ->
                                                                        Toast.makeText(QRScannerActivity.this, "Failed to mark attendance.", Toast.LENGTH_SHORT).show());
                                                    } else {
                                                        Toast.makeText(QRScannerActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                                                    }
                                                    finish();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Toast.makeText(QRScannerActivity.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            });
                                            break;
                                        }
                                    }
                                }

                                if (!valid) {
                                    Toast.makeText(QRScannerActivity.this, "You are too far or QR expired.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                } else {
                    Toast.makeText(QRScannerActivity.this, "Unable to get your location.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(QRScannerActivity.this, "Location permission not granted.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {}
    };

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        }
    }
}
