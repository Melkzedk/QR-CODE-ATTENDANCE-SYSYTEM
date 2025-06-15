package com.example.finalyearproject;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AnnouncementsActivity extends AppCompatActivity {

    private ListView announcementsListView;
    private ArrayList<String> messageList;
    private ArrayAdapter<String> adapter;

    private DatabaseReference messagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        announcementsListView = findViewById(R.id.announcementsListView);
        messageList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
        announcementsListView.setAdapter(adapter);

        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        fetchMessages();
    }

    private void fetchMessages() {
        messagesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                messageList.clear();

                for (DataSnapshot regSnapshot : task.getResult().getChildren()) {
                    // Loop through each reg number (e.g. 0649B)
                    for (DataSnapshot messageSnapshot : regSnapshot.getChildren()) {
                        String title = messageSnapshot.child("title").getValue(String.class);
                        String body = messageSnapshot.child("body").getValue(String.class);

                        if (title != null && body != null) {
                            messageList.add("📌 " + title + "\n" + body);
                        }
                    }
                }

                adapter.notifyDataSetChanged();

                if (messageList.isEmpty()) {
                    Toast.makeText(this, "No announcements available", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Failed to load announcements", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
