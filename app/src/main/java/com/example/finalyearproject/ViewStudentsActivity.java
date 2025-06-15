package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewStudentsActivity extends AppCompatActivity {

    private ListView studentListView;
    private ArrayList<Student> studentList;
    private StudentAdapter adapter;
    private DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_students);

        studentListView = findViewById(R.id.studentListView);
        studentList = new ArrayList<>();
        studentsRef = FirebaseDatabase.getInstance().getReference("Users/Students");

        // Pass the DatabaseReference to the adapter
        adapter = new StudentAdapter(this, studentList, studentsRef);
        studentListView.setAdapter(adapter);

        loadStudents();
    }

    private void loadStudents() {
        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Student student = data.getValue(Student.class);
                    if (student != null) {
                        student.setKey(data.getKey()); // Set Firebase key
                        studentList.add(student);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewStudentsActivity.this, "Failed to load students: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
