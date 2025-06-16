package com.example.finalyearproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<Student> {
    private DatabaseReference studentsRef;

    public StudentAdapter(Context context, List<Student> students, DatabaseReference studentsRef) {
        super(context, 0, students);
        this.studentsRef = studentsRef;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Student student = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);

        text1.setText(student.getName() + " (" + student.getRegNumber() + ")");
        text2.setText("Dept: " + student.getDepartment() + ", Course: " + student.getCourse());

        return convertView;
    }
}
