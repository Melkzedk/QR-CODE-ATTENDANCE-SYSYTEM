package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.util.ArrayList;

public class ViewStudentsActivity extends AppCompatActivity {

    ListView studentListView;
    ArrayList<Student> studentList;
    StudentAdapter adapter;
    DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_students);

        studentListView = findViewById(R.id.studentListView);
        studentList = new ArrayList<>();
        adapter = new StudentAdapter(this, studentList);
        studentListView.setAdapter(adapter);

        studentsRef = FirebaseDatabase.getInstance().getReference("Users/Students");

        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Student student = data.getValue(Student.class);
                    if (student != null) {
                        student.setKey(data.getKey()); // assuming you add a key field for Firebase key
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
