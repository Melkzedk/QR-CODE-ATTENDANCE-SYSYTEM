package com.example.finalyearproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    EditText loginRegNumber, loginPassword;
    Button loginBtn;
    TextView registerRedirect;

    DatabaseReference usersRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginRegNumber = findViewById(R.id.loginRegNumber);
        loginPassword = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        registerRedirect = findViewById(R.id.registerRedirect);

        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(v -> {
            String reg = loginRegNumber.getText().toString().trim();
            String pass = loginPassword.getText().toString().trim();

            if (reg.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Admin login
            if (reg.equalsIgnoreCase("admin") && pass.equals("admin123")) {
                saveSession("admin", "admin");
                Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AdminDashboardActivity.class));
                finish();
                return;
            }

            // Check Lecturers
            usersRef.child("Lecturers").orderByChild("id").equalTo(reg)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot data : snapshot.getChildren()) {
                                    String dbPass = data.child("password").getValue(String.class);
                                    if (dbPass != null && dbPass.equals(pass)) {
                                        saveSession("lecturer", reg);
                                        Toast.makeText(LoginActivity.this, "Lecturer login successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, LecturerDashboardActivity.class));
                                        finish();
                                        return;
                                    }
                                }
                                Toast.makeText(LoginActivity.this, "Wrong password for lecturer", Toast.LENGTH_SHORT).show();
                            } else {
                                // First try FirebaseAuth for self-registered students
                                String authEmail = reg + "@qrcode.edu";

                                mAuth.signInWithEmailAndPassword(authEmail, pass)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                if (user != null) {
                                                    String uid = user.getUid();

                                                    usersRef.child("Students").child(uid)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot userSnap) {
                                                                    if (userSnap.exists()) {
                                                                        saveSession("student", uid);
                                                                        Toast.makeText(LoginActivity.this, "Student login successful", Toast.LENGTH_SHORT).show();
                                                                        startActivity(new Intent(LoginActivity.this, StudentDashboardActivity.class));
                                                                        finish();
                                                                    } else {
                                                                        Toast.makeText(LoginActivity.this, "Student record not found", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            } else {
                                                // If FirebaseAuth fails, try admin-added student login via database
                                                usersRef.child("Students").orderByChild("regNumber").equalTo(reg)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.exists()) {
                                                                    for (DataSnapshot data : snapshot.getChildren()) {
                                                                        String dbPass = data.child("password").getValue(String.class);
                                                                        if (dbPass != null && dbPass.equals(pass)) {
                                                                            String studentId = data.getKey(); // This is the random key
                                                                            saveSession("student", studentId);
                                                                            Toast.makeText(LoginActivity.this, "Student login successful", Toast.LENGTH_SHORT).show();
                                                                            startActivity(new Intent(LoginActivity.this, StudentDashboardActivity.class));
                                                                            finish();
                                                                            return;
                                                                        }
                                                                    }
                                                                    Toast.makeText(LoginActivity.this, "Wrong password for student", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(LoginActivity.this, "No user found", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                                Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        registerRedirect.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void saveSession(String userType, String id) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("userType", userType)
                .putString("userId", id)
                .apply();
    }
}
