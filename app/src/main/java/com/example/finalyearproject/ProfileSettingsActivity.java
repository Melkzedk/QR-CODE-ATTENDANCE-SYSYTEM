package com.example.finalyearproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ProfileSettingsActivity extends AppCompatActivity {

    private EditText nameEditText, regNoEditText, emailEditText, passwordEditText, phoneEditText;
    private Button updateButton;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        nameEditText = findViewById(R.id.nameEditText);
        regNoEditText = findViewById(R.id.regNoEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        updateButton = findViewById(R.id.updateButton);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users").child("Students");

        loadProfile();

        updateButton.setOnClickListener(v -> updateProfile());
    }

    private void loadProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No logged-in user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String regNo = snapshot.child("regNumber").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String email = currentUser.getEmail();

                    nameEditText.setText(name != null ? name : "");
                    regNoEditText.setText(regNo != null ? regNo : "");
                    phoneEditText.setText(phone != null ? phone : "");
                    emailEditText.setText(email != null ? email : "");
                } else {
                    Toast.makeText(ProfileSettingsActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileSettingsActivity.this, "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        String newEmail = emailEditText.getText().toString().trim();
        String newPassword = passwordEditText.getText().toString().trim();
        String newPhone = phoneEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newEmail) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No logged-in user", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update email
        user.updateEmail(newEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Email updated", Toast.LENGTH_SHORT).show();

                // Update password
                user.updatePassword(newPassword).addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Password update failed", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(this, "Email update failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Update phone in Realtime Database
        String uid = user.getUid();
        usersRef.child(uid).child("phone").setValue(newPhone).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Phone updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update phone", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
