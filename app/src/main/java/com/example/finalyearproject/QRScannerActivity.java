package com.example.finalyearproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QRScannerActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 101;

    private DecoratedBarcodeView barcodeScannerView;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        barcodeScannerView = findViewById(R.id.barcode_scanner);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Attendance");

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            startScanner();
        }
    }

    private void startScanner() {
        barcodeScannerView.resume();
        barcodeScannerView.decodeSingle(callback);
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            String qrData = result.getText(); // e.g., "CS101|1718112103500"
            String uid = mAuth.getCurrentUser().getUid();

            String[] parts = qrData.split("\\|");
            if (parts.length != 2) {
                Toast.makeText(QRScannerActivity.this, "Invalid QR format", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String courseCode = parts[0];
            String timestamp = parts[1];

            DatabaseReference attendancePath = dbRef.child(courseCode).child(timestamp).child(uid);

            attendancePath.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(QRScannerActivity.this, "Attendance already marked!", Toast.LENGTH_SHORT).show();
                    } else {
                        attendancePath.setValue(true)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(QRScannerActivity.this, "Attendance marked!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(QRScannerActivity.this, "Failed to mark attendance.", Toast.LENGTH_SHORT).show());
                    }
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(QRScannerActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
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
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
