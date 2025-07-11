//Send Message Activity

package com.example.finalyearproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SendMessageActivity extends AppCompatActivity {

    private EditText regNoEditText, titleEditText, bodyEditText;
    private Button sendMessageBtn;

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        // Initialize views
        regNoEditText = findViewById(R.id.regNoEditText);
        titleEditText = findViewById(R.id.titleEditText);
        bodyEditText = findViewById(R.id.bodyEditText);
        sendMessageBtn = findViewById(R.id.sendMessageBtn);

        // Reference to Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference();

        sendMessageBtn.setOnClickListener(v -> {
            String regNo = regNoEditText.getText().toString().trim();
            String title = titleEditText.getText().toString().trim();
            String body = bodyEditText.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(body)) {
                Toast.makeText(this, "Please fill in both title and message body.", Toast.LENGTH_SHORT).show();
                return;
            }

            sendMessageToFirebase(regNo, title, body);
        });
    }

    private void sendMessageToFirebase(String regNo, String title, String body) {
        HashMap<String, Object> messageData = new HashMap<>();
        messageData.put("title", title);
        messageData.put("body", body);
        messageData.put("timestamp", System.currentTimeMillis());

        if (TextUtils.isEmpty(regNo)) {
            // Send to all students
            String key = databaseRef.child("messages").push().getKey();
            databaseRef.child("messages").child("all").child(key).setValue(messageData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Message sent to all students.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show());
        } else {
            // Send to specific student
            String key = databaseRef.child("messages").child(regNo).push().getKey();
            databaseRef.child("messages").child(regNo).child(key).setValue(messageData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Message sent to " + regNo, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to send message to " + regNo, Toast.LENGTH_SHORT).show());
        }

        // Optionally clear fields
        regNoEditText.setText("");
        titleEditText.setText("");
        bodyEditText.setText("");
    }
}
