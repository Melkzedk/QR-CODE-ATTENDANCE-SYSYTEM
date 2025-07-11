//Submit Assignment Activity
package com.example.finalyearproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class SubmitAssignmentActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;
    private TextView selectedFileText;
    private Button chooseFileBtn, uploadFileBtn;
    private String regNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_assignment);

        // ✅ Load regNumber (actually userId) from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        regNumber = prefs.getString("userId", null);  // fixed from "regNumber" to "userId"

        if (regNumber == null || regNumber.isEmpty()) {
            Toast.makeText(this, "User not logged in. Missing regNumber.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        selectedFileText = findViewById(R.id.selectedFileText);
        chooseFileBtn = findViewById(R.id.chooseFileBtn);
        uploadFileBtn = findViewById(R.id.uploadFileBtn);

        chooseFileBtn.setOnClickListener(v -> openFilePicker());

        uploadFileBtn.setOnClickListener(v -> {
            if (fileUri != null) {
                uploadFile(fileUri);
            } else {
                Toast.makeText(this, "Please choose a file first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            selectedFileText.setText("Selected: " + fileUri.getLastPathSegment());
            Toast.makeText(this, "File selected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(Uri fileUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = "assignments/" + regNumber + "_" + timestamp;

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        progressDialog.dismiss();
                        String downloadUrl = uri.toString();
                        Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show();

                        // ✅ Save to Realtime Database
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("submissions");

                        Map<String, Object> fileData = new HashMap<>();
                        fileData.put("fileUrl", downloadUrl);
                        fileData.put("fileName", fileUri.getLastPathSegment());
                        fileData.put("timestamp", timestamp);

                        dbRef.child(regNumber).push().setValue(fileData) // push to allow multiple uploads
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "File saved in DB!", Toast.LENGTH_SHORT).show();
                                        selectedFileText.setText("Upload complete.");
                                    } else {
                                        Toast.makeText(this, "Failed to save to DB", Toast.LENGTH_LONG).show();
                                    }
                                });

                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to get file URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                })
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%...");
                });
    }
}
