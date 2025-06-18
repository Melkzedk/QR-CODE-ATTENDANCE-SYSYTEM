package com.example.finalyearproject;

public class StudentAttendance {
    public String name;
    public String regNumber;
    public long timestamp;

    public StudentAttendance() {
        // Default constructor required for calls to DataSnapshot.getValue(StudentAttendance.class)
    }

    public StudentAttendance(String name, String regNumber, long timestamp) {
        this.name = name;
        this.regNumber = regNumber;
        this.timestamp = timestamp;
    }
}
