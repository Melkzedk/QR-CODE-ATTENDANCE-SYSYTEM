package com.example.finalyearproject;

public class Student {
    public String id;
    public String name;
    public String regNumber;
    public String email;
    public String department;
    public String course;
    public String status;  // Tracks status: active / deactivated

    private String key;  // Firebase key (not stored in DB)

    // Required empty constructor for Firebase
    public Student() {
    }

    // Constructor with all fields
    public Student(String id, String name, String regNumber, String email, String department, String course, String status) {
        this.id = id;
        this.name = name;
        this.regNumber = regNumber;
        this.email = email;
        this.department = department;
        this.course = course;
        this.status = status;
    }

    // Constructor without status (default to active)
    public Student(String id, String name, String regNumber, String email, String department, String course) {
        this(id, name, regNumber, email, department, course, "active");
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

    public String getStatus() {
        return status;
    }

    public String getKey() {
        return key;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
