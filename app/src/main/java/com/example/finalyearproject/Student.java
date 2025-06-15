package com.example.finalyearproject;

public class Student {
    public String id;
    public String name;
    public String regNumber;
    public String email;
    public String department;
    public String course;
    public String status;  // For deactivation status (optional but useful)

    private String key;  // Firebase key (used internally, not stored in DB)

    // Required empty constructor for Firebase
    public Student() {
    }

    public Student(String id, String name, String regNumber, String email, String department, String course) {
        this.id = id;
        this.name = name;
        this.regNumber = regNumber;
        this.email = email;
        this.department = department;
        this.course = course;
        this.status = "active"; // default status
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getRegNumber() { return regNumber; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public String getCourse() { return course; }
    public String getStatus() { return status; }

    // Setters
    public void setStatus(String status) { this.status = status; }

    // Key
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}
