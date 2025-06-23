---

# 📱 QR Code Attendance System

A smart, secure, and location-aware attendance management system for universities and colleges. Students mark attendance by scanning a QR code displayed by the lecturer. The system ensures authenticity using location and timestamp verification, while also providing features for students, lecturers, and administrators.

---

## 🔍 Overview

The QR Code Attendance System is a mobile-based platform that streamlines and secures the attendance process. It eliminates the need for paper-based registers and unreliable biometric systems by using QR codes, GPS location validation, and real-time notifications.

---

## 🚀 Features

### 🎓 Student

* ✅ Mark attendance by scanning QR code
* 📊 Check attendance percentage
* 📅 View attendance history and timetable
* 📥 Download attendance report
* 📢 View announcements
* 📝 Submit assignments
* 📩 Contact lecturers
* ⚙️ Manage profile settings

### 👨‍🏫 Lecturer

* ✅ Generate and display QR codes for attendance
* 🌍 Save current location (latitude, longitude) with each QR
* 📬 Send messages to students
* 📚 Add and manage courses
* 📈 View and export attendance reports
* 📥 View student submissions
* 🗓️ View timetable

### 👨‍💼 Admin

* 👨‍🎓 Add/view students
* 👨‍🏫 Add/view lecturers
* 📚 Add/manage courses
* 🗓️ Create and manage timetable

---

## 📲 How It Works

1. **Lecturer generates a QR code** before class, which saves their current GPS location and timestamp.
2. **Student scans the QR code** using the app.
3. The system:

   * Checks if the QR is recent (to avoid reusing old codes).
   * Validates if the student is physically near the lecturer using location data.
4. **Attendance is marked automatically** once validations pass.
5. Both student and lecturer **receive reminders 30 minutes** before class starts.

---

## ✅ Why Use This Over Manual or Biometric Systems?

| Feature                        | Manual Attendance | Biometric Systems  | **QR Code Attendance System**         |
| ------------------------------ | ----------------- | ------------------ | ------------------------------------- |
| Speed                          | Slow              | Medium             | ⚡ Fast                                |
| Tamper-proof                   | ❌ Easily faked    | ❌ Can be bypassed  | ✅ GPS + Timestamp validation          |
| Contactless                    | ✅                 | ❌ Touch required   | ✅ Completely contactless              |
| Cost                           | Low               | 💸 High setup cost | 💰 Low-cost (uses phone cam)          |
| Real-time data & reports       | ❌ Manual entry    | ✅                  | ✅ Instant                             |
| Reminders & communication      | ❌ None            | ❌ None             | ✅ Built-in notification and messaging |
| Assignment & Timetable Support | ❌ None            | ❌ None             | ✅ Included                            |

---

## 🛠️ Tech Stack (suggested)

* **Android** (Java/Kotlin)
* **Firebase** (Realtime Database, Cloud Messaging)
* **Google Maps API** (for location services)
* **QR Code Libraries** (e.g., ZXing or ML Kit)
* **Firebase Authentication**

---

## 📂 Project Structure

```plaintext
📁 finalyearproject/
├── Activities/
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── QRScannerActivity.java
│   └── DashboardActivity.java
├── Models/
│   ├── User.java
│   ├── Course.java
│   └── AttendanceRecord.java
├── Utils/
│   └── LocationUtils.java
├── layout/
│   └── *.xml
└── AndroidManifest.xml
```

---

## 📌 Future Enhancements

* AI-based cheating detection (e.g., repeated QR scans in exact locations)
* Offline mode with sync later
* Facial recognition hybrid with QR
* Lecturer location spoof protection

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙌 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

---

## 💬 Contact

For any queries or suggestions:

📧 [melkywafula29@gmail.com](mailto:melkywafula29@gmail.com)
📱 +254-746365629


Screenshots
## 📸 Screenshots

### 🔹 Home Screen
![Home](https://raw.githubusercontent.com/Melkzedk/QR-CODE-ATTENDANCE-SYSYTEM/9a6bc6f57cc2670ae313de4bf6880b18722ac47d/Screenshots/WhatsApp%20Image%202025-06-23%20at%2012.22.51_e591e294.jpg)

### 🔹 Attendance QR
![QR](https://raw.githubusercontent.com/Melkzedk/QR-CODE-ATTENDANCE-SYSYTEM/9a6bc6f57cc2670ae313de4bf6880b18722ac47d/Screenshots/WhatsApp%20Image%202025-06-23%20at%2012.22.51_b1e70a25.jpg)

### 🔹 Location Prompt
![Location](https://raw.githubusercontent.com/Melkzedk/QR-CODE-ATTENDANCE-SYSYTEM/9a6bc6f57cc2670ae313de4bf6880b18722ac47d/Screenshots/WhatsApp%20Image%202025-06-23%20at%2012.22.52_5697a898.jpg)

### 🔹 Camera Preview
![Camera](https://raw.githubusercontent.com/Melkzedk/QR-CODE-ATTENDANCE-SYSYTEM/9a6bc6f57cc2670ae313de4bf6880b18722ac47d/Screenshots/WhatsApp%20Image%202025-06-23%20at%2012.22.52_a34a7b2e.jpg)

### 🔹 Scanning QR
![Scanning](https://raw.githubusercontent.com/Melkzedk/QR-CODE-ATTENDANCE-SYSYTEM/9a6bc6f57cc2670ae313de4bf6880b18722ac47d/Screenshots/WhatsApp%20Image%202025-06-23%20at%2012.22.53_a6cab187.jpg)

### 🔹 Attendance Confirmation
![Confirmation](https://raw.githubusercontent.com/Melkzedk/QR-CODE-ATTENDANCE-SYSYTEM/9a6bc6f57cc2670ae313de4bf6880b18722ac47d/Screenshots/WhatsApp%20Image%202025-06-23%20at%2012.22.53_836e004b.jpg)

### 🔹 Dashboard
![Dashboard](https://raw.githubusercontent.com/Melkzedk/QR-CODE-ATTENDANCE-SYSYTEM/9a6bc6f57cc2670ae313de4bf6880b18722ac47d/Screenshots/WhatsApp%20Image%202025-06-23%20at%2012.22.52_24039e05.jpg)

---
