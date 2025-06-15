package com.example.finalyearproject;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<Student> {
    private Context context;
    private List<Student> students;
    private DatabaseReference studentsRef;  // Added ref

    // Constructor updated to accept DatabaseReference
    public StudentAdapter(Context context, List<Student> students, DatabaseReference studentsRef) {
        super(context, 0, students);
        this.context = context;
        this.students = students;
        this.studentsRef = studentsRef;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false);
        }

        Student student = students.get(position);

        TextView textName = convertView.findViewById(R.id.textStudentName);
        Button btnEdit = convertView.findViewById(R.id.btnEdit);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);
        Button btnDeactivate = convertView.findViewById(R.id.btnDeactivate);

        textName.setText(student.getName());

        btnEdit.setOnClickListener(v -> {
            // Handle edit logic (launch EditStudentActivity)
            Toast.makeText(context, "Edit " + student.getName(), Toast.LENGTH_SHORT).show();
            // You would use an Intent here
        });

        btnDelete.setOnClickListener(v -> showConfirmDialog("Delete", student));
        btnDeactivate.setOnClickListener(v -> showConfirmDialog("Deactivate", student));

        return convertView;
    }

    private void showConfirmDialog(String action, Student student) {
        new AlertDialog.Builder(context)
                .setTitle(action + " Student")
                .setMessage("Do you want to " + action.toLowerCase() + " " + student.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (action.equals("Delete")) {
                        deleteStudent(student);
                    } else if (action.equals("Deactivate")) {
                        deactivateStudent(student);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteStudent(Student student) {
        studentsRef.child(student.getKey()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Student deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deactivateStudent(Student student) {
        studentsRef.child(student.getKey()).child("status").setValue("deactivated")
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Student deactivated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Deactivate failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
