package com.example.finalyearproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddCourseActivity extends AppCompatActivity {

    private EditText courseCodeEditText, courseNameEditText;
    private Button addCourseButton;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        courseCodeEditText = findViewById(R.id.courseCodeEditText);
        courseNameEditText = findViewById(R.id.courseNameEditText);
        addCourseButton = findViewById(R.id.addCourseButton);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        addCourseButton.setOnClickListener(v -> {
            String code = courseCodeEditText.getText().toString().trim();
            String name = courseNameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(code) || TextUtils.isEmpty(name)) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String lecturerId = mAuth.getCurrentUser().getUid();
            Map<String, Object> course = new HashMap<>();
            course.put("courseCode", code);
            course.put("courseName", name);
            course.put("lecturerId", lecturerId);

            db.collection("courses").add(course)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add course: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });
    }
}
