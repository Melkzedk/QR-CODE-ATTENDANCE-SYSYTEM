//TimeTableAdapter Activity

package com.example.finalyearproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    private List<TimetableEntry> timetableList;

    public TimetableAdapter(List<TimetableEntry> timetableList) {
        this.timetableList = timetableList;
    }

    @NonNull
    @Override
    public TimetableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timetable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimetableAdapter.ViewHolder holder, int position) {
        TimetableEntry entry = timetableList.get(position);

        holder.courseCodeText.setText("Course Code: " + entry.courseCode);
        holder.courseNameText.setText("Course: " + entry.courseName);
        holder.lecturerText.setText("Lecturer: " + entry.lecturer);
        holder.timeText.setText("Time: " + entry.startTime + " - " + entry.endTime);
        holder.locationText.setText("Location: " + entry.location);
        holder.dayText.setText("Day: " + entry.day);
    }

    @Override
    public int getItemCount() {
        return timetableList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseCodeText, courseNameText, lecturerText, timeText, locationText, dayText;

        public ViewHolder(View itemView) {
            super(itemView);
            courseCodeText = itemView.findViewById(R.id.courseCodeText);
            courseNameText = itemView.findViewById(R.id.courseNameText);
            lecturerText = itemView.findViewById(R.id.lecturerText);
            timeText = itemView.findViewById(R.id.timeText);
            locationText = itemView.findViewById(R.id.locationText);
            dayText = itemView.findViewById(R.id.dayText);
        }
    }
}
