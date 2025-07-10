//AddCourseActivity.java
package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddCourseActivity extends AppCompatActivity {

    private EditText courseCodeEditText, courseNameEditText;
    private Button addCourseButton, backToDashboardBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference courseRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        courseCodeEditText = findViewById(R.id.courseCodeEditText);
        courseNameEditText = findViewById(R.id.courseNameEditText);
        addCourseButton = findViewById(R.id.addCourseButton);
        backToDashboardBtn = findViewById(R.id.backToDashboardBtn);

        mAuth = FirebaseAuth.getInstance();
        courseRef = FirebaseDatabase.getInstance().getReference("courses");

        addCourseButton.setOnClickListener(v -> {
            String code = courseCodeEditText.getText().toString().trim();
            String name = courseNameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(code) || TextUtils.isEmpty(name)) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String courseId = courseRef.push().getKey();
            if (courseId == null) {
                Toast.makeText(this, "Failed to generate course ID", Toast.LENGTH_SHORT).show();
                return;
            }

            String lecturerId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "unknown";

            Map<String, Object> course = new HashMap<>();
            course.put("courseId", courseId);
            course.put("courseCode", code);
            course.put("courseName", name);
            course.put("lecturerId", lecturerId);

            courseRef.child(courseId).setValue(course)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);  // Notify ManageCoursesActivity
                        finish();              // Close and go back
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add course: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        backToDashboardBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, LecturerDashboardActivity.class));
            finish();
        });
    }
}
