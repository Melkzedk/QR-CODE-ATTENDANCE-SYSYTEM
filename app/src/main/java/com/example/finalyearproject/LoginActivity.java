package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    EditText loginRegNumber, loginPassword;
    Button loginBtn;
    TextView registerRedirect;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginRegNumber = findViewById(R.id.loginRegNumber);
        loginPassword = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        registerRedirect = findViewById(R.id.registerRedirect);

        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        loginBtn.setOnClickListener(v -> {
            String reg = loginRegNumber.getText().toString().trim();
            String pass = loginPassword.getText().toString().trim();

            if (reg.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Admin login
            if (reg.equalsIgnoreCase("admin") && pass.equals("admin123")) {
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
                                        Toast.makeText(LoginActivity.this, "Lecturer login successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, LecturerDashboardActivity.class));
                                        finish();
                                        return;
                                    }
                                }
                                Toast.makeText(LoginActivity.this, "Wrong password for lecturer", Toast.LENGTH_SHORT).show();
                            } else {
                                // Check Students
                                usersRef.child("Students").orderByChild("regNumber").equalTo(reg)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot data : snapshot.getChildren()) {
                                                        String dbPass = data.child("password").getValue(String.class);
                                                        if (dbPass != null && dbPass.equals(pass)) {
                                                            Toast.makeText(LoginActivity.this, "Student login successful", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                                                            intent.putExtra("regNumber", reg);
                                                            startActivity(intent);
                                                            finish();
                                                            return;
                                                        }
                                                    }
                                                    Toast.makeText(LoginActivity.this, "Wrong password for student", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "No user found with provided ID/RegNumber", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(LoginActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
