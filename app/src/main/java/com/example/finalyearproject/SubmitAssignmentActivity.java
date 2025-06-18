package com.example.finalyearproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class SubmitAssignmentActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;
    private TextView selectedFileText;
    private Button chooseFileBtn, uploadFileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_assignment);

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
        intent.setType("*/*"); // You can filter by PDF/doc etc.
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            selectedFileText.setText("Selected: " + fileUri.getLastPathSegment());
        }
    }

    private void uploadFile(Uri fileUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String fileName = "assignments/" + userId + "_" + System.currentTimeMillis();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
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
