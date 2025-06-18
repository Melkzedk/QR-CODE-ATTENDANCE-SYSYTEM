// src/com/example/finalyearproject/ViewAssignmentsActivity.java
package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class ViewAssignmentsActivity extends AppCompatActivity {

    ListView assignmentListView;
    ArrayAdapter<String> adapter;
    ArrayList<String> assignmentList;
    DatabaseReference assignmentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_assignment);

        assignmentListView = findViewById(R.id.assignmentListView);
        assignmentList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, assignmentList);
        assignmentListView.setAdapter(adapter);

        assignmentsRef = FirebaseDatabase.getInstance().getReference("Assignments");

        assignmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                assignmentList.clear();

                for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                    String title = assignmentSnapshot.child("title").getValue(String.class);
                    String subject = assignmentSnapshot.child("subject").getValue(String.class);
                    String dueDate = assignmentSnapshot.child("dueDate").getValue(String.class);

                    String item = "Title: " + title + "\nSubject: " + subject + "\nDue: " + dueDate;
                    assignmentList.add(item);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewAssignmentsActivity.this, "Failed to load assignments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
