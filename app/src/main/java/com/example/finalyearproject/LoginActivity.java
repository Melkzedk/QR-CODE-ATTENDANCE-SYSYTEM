// ✅ Updated LoginActivity.java
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

            if (reg.equalsIgnoreCase("admin") && pass.equals("admin1234")) {
                saveSession("admin", "admin", "admin");
                startActivity(new Intent(this, AdminDashboardActivity.class));
                finish();
                return;
            }

            usersRef.child("Lecturers").orderByChild("id").equalTo(reg)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot data : snapshot.getChildren()) {
                                    String dbPass = data.child("password").getValue(String.class);
                                    if (dbPass != null && dbPass.equals(pass)) {
                                        saveSession("lecturer", reg, reg);
                                        startActivity(new Intent(LoginActivity.this, LecturerDashboardActivity.class));
                                        finish();
                                        return;
                                    }
                                }
                                Toast.makeText(LoginActivity.this, "Wrong password for lecturer", Toast.LENGTH_SHORT).show();
                            } else {
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
                                                                        String regNumber = userSnap.child("regNumber").getValue(String.class);
                                                                        saveSession("student", uid, regNumber);
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
                                                usersRef.child("Students").orderByChild("regNumber").equalTo(reg)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.exists()) {
                                                                    for (DataSnapshot data : snapshot.getChildren()) {
                                                                        String dbPass = data.child("password").getValue(String.class);
                                                                        if (dbPass != null && dbPass.equals(pass)) {
                                                                            String studentId = data.getKey();
                                                                            saveSession("student", studentId, reg);
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

    private void saveSession(String userType, String userId, String regNumber) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("userType", userType)
                .putString("userId", userId)
                .putString("regNumber", regNumber)
                .apply();
    }
}