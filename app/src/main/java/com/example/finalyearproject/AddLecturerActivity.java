package com.example.finalyearproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddLecturerActivity extends AppCompatActivity {

    EditText lecturerName, lecturerID, lecturerEmail, lecturerPhone, lecturerDepartment;
    Button btnAddLecturer, btnRemoveLecturer;
    DatabaseReference lecturerRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lecturer);

        lecturerName = findViewById(R.id.lecturerName);
        lecturerID = findViewById(R.id.lecturerID);
        lecturerEmail = findViewById(R.id.lecturerEmail);
        lecturerPhone = findViewById(R.id.lecturerPhone);
        lecturerDepartment = findViewById(R.id.lecturerDepartment);
        btnAddLecturer = findViewById(R.id.btnAddLecturer);
        btnRemoveLecturer = findViewById(R.id.btnRemoveLecturer);

        lecturerRef = FirebaseDatabase.getInstance().getReference("Users/Lecturers");

        btnAddLecturer.setOnClickListener(v -> {
            String name = lecturerName.getText().toString().trim();
            String id = lecturerID.getText().toString().trim();
            String email = lecturerEmail.getText().toString().trim();
            String phone = lecturerPhone.getText().toString().trim();
            String dept = lecturerDepartment.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(id) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(phone) || TextUtils.isEmpty(dept)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Lecturer lecturer = new Lecturer(id, name, email, phone, dept);
            lecturerRef.child(id).setValue(lecturer)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Lecturer Added", Toast.LENGTH_SHORT).show();
                            lecturerName.setText("");
                            lecturerID.setText("");
                            lecturerEmail.setText("");
                            lecturerPhone.setText("");
                            lecturerDepartment.setText("");
                        } else {
                            Toast.makeText(this, "Failed to add lecturer", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnRemoveLecturer.setOnClickListener(v -> {
            String id = lecturerID.getText().toString().trim();
            if (TextUtils.isEmpty(id)) {
                Toast.makeText(this, "Enter Lecturer ID to remove", Toast.LENGTH_SHORT).show();
                return;
            }

            lecturerRef.child(id).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Lecturer Removed", Toast.LENGTH_SHORT).show();
                            lecturerID.setText("");
                            lecturerName.setText("");
                            lecturerEmail.setText("");
                            lecturerPhone.setText("");
                            lecturerDepartment.setText("");
                        } else {
                            Toast.makeText(this, "Failed to remove lecturer", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
