package com.example.finalyearproject;

import android.Manifest;
import android.app.ProgressDialog;
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
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        barcodeScannerView = findViewById(R.id.barcode_scanner);
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Marking attendance...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            startScanner();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }
    }

    private void startScanner() {
        barcodeScannerView.resume();
        barcodeScannerView.decodeSingle(callback);
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            progressDialog.show();

            String qrData = result.getText();
            String uid = mAuth.getCurrentUser().getUid();

            String[] parts = qrData.split("\\|");
            if (parts.length != 2) {
                progressDialog.dismiss();
                Toast.makeText(QRScannerActivity.this, "Invalid QR format", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String sessionId = parts[0];
            long qrTimestamp;

            try {
                qrTimestamp = Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                progressDialog.dismiss();
                Toast.makeText(QRScannerActivity.this, "Invalid timestamp", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(QRScannerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location studentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (studentLocation == null) {
                    progressDialog.dismiss();
                    Toast.makeText(QRScannerActivity.this, "Unable to get location.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                DatabaseReference sessionRef = FirebaseDatabase.getInstance().getReference("attendance_sessions").child(sessionId);
                sessionRef.get().addOnSuccessListener(snap -> {
                    if (!snap.exists()) {
                        progressDialog.dismiss();
                        Toast.makeText(QRScannerActivity.this, "Session not found.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    Long dbTimestamp = snap.child("timestamp").getValue(Long.class);
                    Double lat = snap.child("latitude").getValue(Double.class);
                    Double lng = snap.child("longitude").getValue(Double.class);

                    if (dbTimestamp == null || lat == null || lng == null) {
                        progressDialog.dismiss();
                        Toast.makeText(QRScannerActivity.this, "Incomplete session data.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Check time validity
                    if (Math.abs(dbTimestamp - qrTimestamp) > 60000) {
                        progressDialog.dismiss();
                        Toast.makeText(QRScannerActivity.this, "QR expired.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Check location proximity
                    Location lecturerLocation = new Location("");
                    lecturerLocation.setLatitude(lat);
                    lecturerLocation.setLongitude(lng);
                    float distance = studentLocation.distanceTo(lecturerLocation);

                    if (distance > 50) {
                        progressDialog.dismiss();
                        Toast.makeText(QRScannerActivity.this, "You are too far from the class.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Fetch student regNumber from Users/Students/uid
                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("Users/Students").child(uid);

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnap) {
                            progressDialog.dismiss();
                            if (!userSnap.exists()) {
                                Toast.makeText(QRScannerActivity.this, "Student not found.", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }

                            String regNumber = userSnap.child("regNumber").getValue(String.class);
                            if (regNumber == null) {
                                Toast.makeText(QRScannerActivity.this, "Missing regNumber.", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }

                            DatabaseReference attendanceRef = sessionRef.child("attendees");

                            attendanceRef.child(regNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Toast.makeText(QRScannerActivity.this, "Already marked.", Toast.LENGTH_LONG).show();
                                    } else {
                                        attendanceRef.child(regNumber).setValue(true);
                                        Toast.makeText(QRScannerActivity.this, "✅ Attendance marked!", Toast.LENGTH_SHORT).show();
                                    }
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(QRScannerActivity.this, "Error saving attendance.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressDialog.dismiss();
                            Toast.makeText(QRScannerActivity.this, "Error loading student.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                });

            } else {
                progressDialog.dismiss();
                Toast.makeText(QRScannerActivity.this, "Location permission not granted.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
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
        if (requestCode == CAMERA_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        }
    }
}
