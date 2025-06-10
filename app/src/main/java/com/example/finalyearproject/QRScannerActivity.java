package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QRScannerActivity extends AppCompatActivity {
    private DecoratedBarcodeView barcodeScannerView;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        subject = getIntent().getStringExtra("subject");
        barcodeScannerView = findViewById(R.id.barcode_scanner);
        barcodeScannerView.decodeSingle(callback);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Attendance");
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            String qrData = result.getText(); // contains lecture's QR value
            String uid = mAuth.getCurrentUser().getUid();

            dbRef.child(subject).child(qrData).child(uid).setValue(true)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(QRScannerActivity.this, "Attendance marked!", Toast.LENGTH_SHORT).show()
                    );
            finish();
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {}
    };
}
