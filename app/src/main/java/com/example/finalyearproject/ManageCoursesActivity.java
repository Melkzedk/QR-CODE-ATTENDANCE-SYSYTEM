package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;
import java.util.ArrayList;

public class ManageCoursesActivity extends AppCompatActivity {

    private ListView courseListView;
    private Button addNewCourseBtn;
    private ArrayList<String> courseList = new ArrayList<>();
    private ArrayList<String> courseIds = new ArrayList<>();  // Track IDs
    private ArrayAdapter<String> adapter;
    private DatabaseReference coursesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_courses);

        courseListView = findViewById(R.id.courseListView);
        addNewCourseBtn = findViewById(R.id.addNewCourseBtn);
        coursesRef = FirebaseDatabase.getInstance().getReference("courses");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, courseList);
        courseListView.setAdapter(adapter);

        loadCourses();

        addNewCourseBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ManageCoursesActivity.this, AddCourseActivity.class);
            startActivityForResult(intent, 1);  // Use for refresh
        });

        courseListView.setOnItemClickListener((parent, view, position, id) -> {
            String display = courseList.get(position);
            String courseId = courseIds.get(position);
            showManageOptionsDialog(courseId, display);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadCourses();  // Refresh after add
        }
    }

    private void loadCourses() {
        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear();
                courseIds.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String code = snap.child("courseCode").getValue(String.class);
                    String name = snap.child("courseName").getValue(String.class);

                    if (code != null && name != null) {
                        courseList.add(code + " - " + name);
                        courseIds.add(snap.getKey());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageCoursesActivity.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showManageOptionsDialog(String courseId, String display) {
        String[] options = {"Edit Course", "Delete Course"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manage " + display);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showEditCourseDialog(courseId);
            } else if (which == 1) {
                deleteCourse(courseId);
            }
        });
        builder.show();
    }

    private void showEditCourseDialog(String courseId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Course Name");

        final EditText input = new EditText(this);
        input.setHint("New Course Name");
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                coursesRef.child(courseId).child("courseName").setValue(newName)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(ManageCoursesActivity.this, "Course updated", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(ManageCoursesActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteCourse(String courseId) {
        coursesRef.child(courseId).removeValue()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(ManageCoursesActivity.this, "Course deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(ManageCoursesActivity.this, "Delete failed", Toast.LENGTH_SHORT).show());
    }
}
