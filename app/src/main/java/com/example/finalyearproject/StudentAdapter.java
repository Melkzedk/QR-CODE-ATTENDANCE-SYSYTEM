package com.example.finalyearproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<Student> {
    private Context context;
    private List<Student> students;

    public StudentAdapter(Context context, List<Student> students) {
        super(context, 0, students);
        this.context = context;
        this.students = students;
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
            // Handle edit logic here
            Toast.makeText(context, "Edit " + student.getName(), Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            // Handle delete logic here
            Toast.makeText(context, "Delete " + student.getName(), Toast.LENGTH_SHORT).show();
        });

        btnDeactivate.setOnClickListener(v -> {
            // Handle deactivate logic here
            Toast.makeText(context, "Deactivate " + student.getName(), Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }
}
