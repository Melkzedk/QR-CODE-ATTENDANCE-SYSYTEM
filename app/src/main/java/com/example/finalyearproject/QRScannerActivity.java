package com.example.finalyearproject;

import androidx.appcompat.app.AppCompatActivity;

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
