package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class ViewTimetableActivity extends AppCompatActivity {

    RecyclerView timetableRecyclerView;
    TimetableAdapter adapter;
    List<TimetableEntry> timetableList;
    DatabaseReference timetableRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_timetable);

        timetableRecyclerView = findViewById(R.id.timetableRecyclerView);
        timetableRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        timetableList = new ArrayList<>();
        adapter = new TimetableAdapter(timetableList);
        timetableRecyclerView.setAdapter(adapter);

        timetableRef = FirebaseDatabase.getInstance().getReference("timetable");

        // Fetch data from Firebase
        timetableRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                timetableList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    TimetableEntry entry = data.getValue(TimetableEntry.class);
                    timetableList.add(entry);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewTimetableActivity.this,
                        "Failed to load timetable", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
