package com.example.finalyearproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactLecturerActivity extends AppCompatActivity {

    private Spinner lecturerSpinner;
    private EditText messageEditText;
    private Button sendMessageBtn;

    private DatabaseReference lecturersRef;
    private ArrayList<String> lecturerNames = new ArrayList<>();
    private HashMap<String, String> nameToIdMap = new HashMap<>(); // name -> id mapping

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_lecturer);

        lecturerSpinner = findViewById(R.id.lecturerSpinner);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageBtn = findViewById(R.id.sendMessageBtn);

        // FIXED: Correct Firebase reference
        lecturersRef = FirebaseDatabase.getInstance().getReference("Users").child("Lecturers");

        loadLecturers();

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedLecturer = lecturerSpinner.getSelectedItem().toString();
                String lecturerId = nameToIdMap.get(selectedLecturer);
                String message = messageEditText.getText().toString().trim();

                if (message.isEmpty()) {
                    Toast.makeText(ContactLecturerActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (lecturerId == null) {
                    Toast.makeText(ContactLecturerActivity.this, "Invalid lecturer selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                sendMessageToLecturer(lecturerId, selectedLecturer, message);
            }
        });
    }

    private void loadLecturers() {
        lecturersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lecturerNames.clear();
                nameToIdMap.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    String name = data.child("name").getValue(String.class);
                    String id = data.child("id").getValue(String.class);

                    // DEBUG: Log values
                    System.out.println("Lecturer: " + name + " | ID: " + id);

                    if (name != null && id != null) {
                        lecturerNames.add(name);
                        nameToIdMap.put(name, id);
                    }
                }

                if (!lecturerNames.isEmpty()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            ContactLecturerActivity.this,
                            android.R.layout.simple_spinner_item,
                            lecturerNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    lecturerSpinner.setAdapter(adapter);
                } else {
                    Toast.makeText(ContactLecturerActivity.this, "No lecturers found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ContactLecturerActivity.this, "Failed to load lecturers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessageToLecturer(String lecturerId, String lecturerName, String message) {
        DatabaseReference messageRef = FirebaseDatabase.getInstance()
                .getReference("messages").child(lecturerId).push();

        HashMap<String, Object> messageData = new HashMap<>();
        messageData.put("title", "New Message");
        messageData.put("body", message);
        messageData.put("timestamp", System.currentTimeMillis());

        messageRef.setValue(messageData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Message sent to " + lecturerName, Toast.LENGTH_SHORT).show();
                messageEditText.setText("");
            } else {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
