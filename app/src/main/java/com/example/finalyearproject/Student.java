package com.example.finalyearproject;

public class Student {
    public String id;
    public String name;
    public String regNumber;
    public String email;
    public String department;
    public String course;

    private String key;  // 🔑 Firebase key for this student (not stored in DB, used in app)

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
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public String getCourse() {
        return course;
    }

    // Getter and Setter for key 🔑
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    // Optional: Add setters for other fields if you want to allow updating them
}
