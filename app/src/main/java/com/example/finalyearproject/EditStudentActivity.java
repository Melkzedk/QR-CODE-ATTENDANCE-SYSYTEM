package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class EditStudentActivity extends AppCompatActivity {

    private EditText editName, editRegNumber, editEmail, editDepartment, editCourse;
    private Button btnSave;

    private DatabaseReference studentRef;
    private String studentKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);

        editName = findViewById(R.id.editName);
        editRegNumber = findViewById(R.id.editRegNumber);
        editEmail = findViewById(R.id.editEmail);
        editDepartment = findViewById(R.id.editDepartment);
        editCourse = findViewById(R.id.editCourse);
        btnSave = findViewById(R.id.btnSave);

        studentKey = getIntent().getStringExtra("studentKey");

        if (studentKey == null || studentKey.isEmpty()) {
            Toast.makeText(this, "Invalid student key!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        studentRef = FirebaseDatabase.getInstance().getReference("Users/Students").child(studentKey);

        loadStudentData();

        btnSave.setOnClickListener(v -> saveStudentData());
    }

    private void loadStudentData() {
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Student student = snapshot.getValue(Student.class);
                if (student != null) {
                    editName.setText(student.getName());
                    editRegNumber.setText(student.getRegNumber());
                    editEmail.setText(student.getEmail());
                    editDepartment.setText(student.getDepartment());
                    editCourse.setText(student.getCourse());
                } else {
                    Toast.makeText(EditStudentActivity.this, "Student not found!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditStudentActivity.this, "Failed to load: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveStudentData() {
        String name = editName.getText().toString().trim();
        String regNumber = editRegNumber.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String department = editDepartment.getText().toString().trim();
        String course = editCourse.getText().toString().trim();

        if (name.isEmpty() || regNumber.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name, Reg Number, and Email are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        studentRef.child("name").setValue(name);
        studentRef.child("regNumber").setValue(regNumber);
        studentRef.child("email").setValue(email);
        studentRef.child("department").setValue(department);
        studentRef.child("course").setValue(course)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditStudentActivity.this, "Student updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(EditStudentActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
