//TimeTableEntry Activity
package com.example.finalyearproject;

public class TimetableEntry {
    public String courseCode, courseName, lecturer, startTime, endTime, location, day;

    public TimetableEntry() {
        // Needed for Firebase
    }

    public TimetableEntry(String courseCode, String courseName, String lecturer,
                          String startTime, String endTime, String location, String day, String status) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.lecturer = lecturer;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.day = day;
    }
}
