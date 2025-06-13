package com.example.finalyearproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddStudentActivity extends AppCompatActivity {

    EditText studentName, regNumber;
    Button addStudentBtn;
    DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        studentName = findViewById(R.id.studentName);
        regNumber = findViewById(R.id.regNumber);
        addStudentBtn = findViewById(R.id.addStudentBtn);

        studentsRef = FirebaseDatabase.getInstance().getReference("Users/Students");

        addStudentBtn.setOnClickListener(v -> {
            String name = studentName.getText().toString();
            String reg = regNumber.getText().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(reg)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                String studentId = studentsRef.push().getKey();
                Student student = new Student(studentId, name, reg);
                studentsRef.child(studentId).setValue(student)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show();
                                studentName.setText("");
                                regNumber.setText("");
                            }
                        });
            }
        });
    }
}
