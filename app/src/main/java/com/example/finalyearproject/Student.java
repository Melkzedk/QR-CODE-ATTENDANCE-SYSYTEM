//Student Model
package com.example.finalyearproject;

public class Student {
    public String id;
    public String name;
    public String regNumber;
    public String email;
    public String department;
    public String course;
    public String password;
    public String status;

    private String key;  // Add this for Firebase key

    public Student() {}

    public Student(String id, String name, String regNumber, String email, String department,
                   String course, String password, String status) {
        this.id = id;
        this.name = name;
        this.regNumber = regNumber;
        this.email = email;
        this.department = department;
        this.course = course;
        this.password = password;
        this.status = status;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getRegNumber() { return regNumber; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public String getCourse() { return course; }
    public String getPassword() { return password; }
    public String getStatus() { return status; }
    public String getKey() { return key; }

    // Setter for key
    public void setKey(String key) { this.key = key; }
}
