package com.example.finalyearproject;

public class Lecturer {
    public String id;
    public String name;
    public String email;
    public String phone;
    public String department;

    public Lecturer() {
        // Needed for Firebase
    }

    public Lecturer(String id, String name, String email, String phone, String department) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.department = department;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDepartment() {
        return department;
    }
}
