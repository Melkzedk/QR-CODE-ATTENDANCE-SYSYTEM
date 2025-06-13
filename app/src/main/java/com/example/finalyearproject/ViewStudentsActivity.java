package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.util.ArrayList;

public class ViewStudentsActivity extends AppCompatActivity {

    ListView studentListView;
    ArrayAdapter<String> adapter;
    ArrayList<String> studentList;
    DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_students);

        studentListView = findViewById(R.id.studentListView);
        studentList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentList);
        studentListView.setAdapter(adapter);

        studentsRef = FirebaseDatabase.getInstance().getReference("Users/Students");

        studentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Student student = data.getValue(Student.class);
                    if (student != null) {
                        studentList.add(student.getName() + " (" + student.getRegNumber() + ")");
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                studentList.add("Failed to load students: " + error.getMessage());
                adapter.notifyDataSetChanged();
            }
        });
    }
}
