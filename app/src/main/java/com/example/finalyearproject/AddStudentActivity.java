//AddStudentActivity.java
package com.example.finalyearproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddStudentActivity extends AppCompatActivity {

    EditText studentName, regNumber, studentEmail, studentDepartment, studentCourse, studentPassword;
    Button addStudentBtn;

    FirebaseAuth mAuth;
    DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        studentName = findViewById(R.id.studentName);
        regNumber = findViewById(R.id.regNumber);
        studentEmail = findViewById(R.id.studentEmail);
        studentDepartment = findViewById(R.id.studentDepartment);
        studentCourse = findViewById(R.id.studentCourse);
        studentPassword = findViewById(R.id.studentPassword);
        addStudentBtn = findViewById(R.id.addStudentBtn);

        mAuth = FirebaseAuth.getInstance();
        studentsRef = FirebaseDatabase.getInstance().getReference("Users").child("Students");

        addStudentBtn.setOnClickListener(v -> {
            String name = studentName.getText().toString().trim();
            String reg = regNumber.getText().toString().trim();
            String email = studentEmail.getText().toString().trim();
            String dept = studentDepartment.getText().toString().trim();
            String course = studentCourse.getText().toString().trim();
            String password = studentPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(reg) || TextUtils.isEmpty(email)
                    || TextUtils.isEmpty(dept) || TextUtils.isEmpty(course) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill all field", Toast.LENGTH_SHORT).show();
                return;
            }

            String authEmail = reg + "@qrcode.edu"; // Simulated email for FirebaseAuth

            mAuth.createUserWithEmailAndPassword(authEmail, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user == null) {
                                Toast.makeText(this, "Failed to get user details", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String uid = user.getUid();

                            Student student = new Student(uid, name, reg, email, dept, course, password, "deactivated");

                            studentsRef.child(uid).setValue(student)
                                    .addOnCompleteListener(saveTask -> {
                                        if (saveTask.isSuccessful()) {
                                            Toast.makeText(this, "✅ Student added successfully", Toast.LENGTH_SHORT).show();
                                            studentName.setText("");
                                            regNumber.setText("");
                                            studentEmail.setText("");
                                            studentDepartment.setText("");
                                            studentCourse.setText("");
                                            studentPassword.setText("");
                                        } else {
                                            Toast.makeText(this, "❌ Failed to save to database", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(this, "❌ Auth error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
