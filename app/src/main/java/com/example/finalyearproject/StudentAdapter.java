package com.example.finalyearproject;

import android.content.Context;
import android.content.Intent;
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
    private DatabaseReference studentsRef;

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
            Intent intent = new Intent(context, EditStudentActivity.class);
            intent.putExtra("studentKey", student.getKey());
            intent.putExtra("name", student.getName());
            intent.putExtra("regNumber", student.getRegNumber());
            intent.putExtra("email", student.getEmail());
            intent.putExtra("department", student.getDepartment());
            intent.putExtra("course", student.getCourse());
            context.startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            if (student.getKey() != null) {
                studentsRef.child(student.getKey()).removeValue()
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(context, "Deleted " + student.getName(), Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(context, "Student key is missing!", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeactivate.setOnClickListener(v -> {
            if (student.getKey() != null) {
                studentsRef.child(student.getKey()).child("status").setValue("deactivated")
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(context, "Deactivated " + student.getName(), Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Deactivate failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(context, "Student key is missing!", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }
}
