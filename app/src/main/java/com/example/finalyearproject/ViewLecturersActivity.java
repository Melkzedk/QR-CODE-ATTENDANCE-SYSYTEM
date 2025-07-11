//ViewLecturersActivity.java
package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class ViewLecturersActivity extends AppCompatActivity {

    ListView lecturerListView;

    ArrayAdapter<String> adapter;
    ArrayList<String> lecturerList;
    DatabaseReference lecturerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lecturers);

        lecturerListView = findViewById(R.id.lecturerListView);
        lecturerList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lecturerList);
        lecturerListView.setAdapter(adapter);

        lecturerRef = FirebaseDatabase.getInstance().getReference("Users/Lecturers");

        lecturerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lecturerList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Lecturer lecturer = data.getValue(Lecturer.class);
                    if (lecturer != null) {
                        lecturerList.add(
                                lecturer.getName() + " (" + lecturer.getId() + ")\n" +
                                        "Email: " + lecturer.getEmail() + ", Phone: " + lecturer.getPhone() + "\n" +
                                        "Dept: " + lecturer.getDepartment()
                        );
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewLecturersActivity.this, "Failed to load lecturers: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
