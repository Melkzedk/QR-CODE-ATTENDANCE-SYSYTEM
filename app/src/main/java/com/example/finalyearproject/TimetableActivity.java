package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class TimetableActivity extends AppCompatActivity {

    EditText courseCodeInput, courseNameInput, lecturerInput, startTimeInput, endTimeInput, locationInput;
    Spinner daySpinner;
    Button btnAddToTimetable;

    DatabaseReference timetableRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        // Initialize inputs
        courseCodeInput = findViewById(R.id.courseCodeInput);
        courseNameInput = findViewById(R.id.courseNameInput);
        lecturerInput = findViewById(R.id.lecturerInput);
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);
        locationInput = findViewById(R.id.locationInput);
        daySpinner = findViewById(R.id.daySpinner);
        btnAddToTimetable = findViewById(R.id.btnAddToTimetable);

        // Setup Spinner with days of the week
        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);

        // Firebase DB reference
        timetableRef = FirebaseDatabase.getInstance().getReference("timetable");

        btnAddToTimetable.setOnClickListener(v -> saveTimetableEntry());
    }

    private void saveTimetableEntry() {
        String courseCode = courseCodeInput.getText().toString().trim();
        String courseName = courseNameInput.getText().toString().trim();
        String lecturer = lecturerInput.getText().toString().trim();
        String startTime = startTimeInput.getText().toString().trim();
        String endTime = endTimeInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String day = daySpinner.getSelectedItem().toString();

        if (courseCode.isEmpty() || courseName.isEmpty() || lecturer.isEmpty()
                || startTime.isEmpty() || endTime.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique ID
        String entryId = timetableRef.push().getKey();

        // Create an entry object
        TimetableEntry entry = new TimetableEntry(
                courseCode, courseName, lecturer, startTime, endTime, location, day
        );

        // Save to Firebase
        timetableRef.child(entryId).setValue(entry)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Timetable saved!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        courseCodeInput.setText("");
        courseNameInput.setText("");
        lecturerInput.setText("");
        startTimeInput.setText("");
        endTimeInput.setText("");
        locationInput.setText("");
        daySpinner.setSelection(0);
    }
}
