package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private DecoratedBarcodeView barcodeScannerView;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        barcodeScannerView = findViewById(R.id.barcode_scanner);
        barcodeScannerView.decodeSingle(callback);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Attendance");
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            String qrData = result.getText(); // e.g., "CS101|1718112103500"
            String uid = mAuth.getCurrentUser().getUid();

            // Split QR data into courseCode and timestamp
            String[] parts = qrData.split("\\|");
            if (parts.length != 2) {
                Toast.makeText(QRScannerActivity.this, "Invalid QR format", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String courseCode = parts[0];
            String timestamp = parts[1];

            DatabaseReference attendancePath = dbRef.child(courseCode).child(timestamp).child(uid);

            // Check if already marked
            attendancePath.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(QRScannerActivity.this, "Attendance already marked!", Toast.LENGTH_SHORT).show();
                    } else {
                        attendancePath.setValue(true)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(QRScannerActivity.this, "Attendance marked!", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(QRScannerActivity.this, "Failed to mark attendance.", Toast.LENGTH_SHORT).show()
                                );
                    }
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(QRScannerActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {}
    };
}
