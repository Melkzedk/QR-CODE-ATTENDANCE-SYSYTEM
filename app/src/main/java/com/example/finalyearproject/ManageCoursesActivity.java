package com.example.finalyearproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;
import java.util.ArrayList;

public class ManageCoursesActivity extends AppCompatActivity {

    ListView courseListView;
    Button addNewCourseBtn;
    ArrayList<String> courseList = new ArrayList<>();
    ArrayAdapter<String> adapter;
    DatabaseReference coursesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_courses);

        courseListView = findViewById(R.id.courseListView);
        addNewCourseBtn = findViewById(R.id.addNewCourseBtn);
        coursesRef = FirebaseDatabase.getInstance().getReference("Courses");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, courseList);
        courseListView.setAdapter(adapter);

        loadCourses();

        addNewCourseBtn.setOnClickListener(v -> showAddCourseDialog());

        courseListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCourse = courseList.get(position);
            showManageOptionsDialog(selectedCourse);
        });
    }

    private void loadCourses() {
        coursesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear();
                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    String courseName = courseSnap.getKey();
                    courseList.add(courseName);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageCoursesActivity.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Course");

        final EditText input = new EditText(this);
        input.setHint("Course Name");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String courseName = input.getText().toString().trim();
            if (!courseName.isEmpty()) {
                coursesRef.child(courseName).setValue(true)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Course added", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to add course", Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showManageOptionsDialog(String courseName) {
        String[] options = {"Edit Course", "Delete Course"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manage " + courseName);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showEditCourseDialog(courseName);
            } else if (which == 1) {
                deleteCourse(courseName);
            }
        });
        builder.show();
    }

    private void showEditCourseDialog(String oldName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Course");

        final EditText input = new EditText(this);
        input.setText(oldName);
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(oldName)) {
                coursesRef.child(oldName).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            coursesRef.child(newName).setValue(true)
                                    .addOnSuccessListener(aVoid1 -> Toast.makeText(this, "Course updated", Toast.LENGTH_SHORT).show());
                        });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteCourse(String courseName) {
        coursesRef.child(courseName).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
    }
}
