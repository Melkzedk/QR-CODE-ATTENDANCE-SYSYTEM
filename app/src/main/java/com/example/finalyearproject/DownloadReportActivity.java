package com.example.finalyearproject;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook; // ✅ HSSF for .xls
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class DownloadReportActivity extends AppCompatActivity {

    private Button downloadPdfBtn, downloadExcelBtn;
    private DatabaseReference attendanceRef;
    private String regNumber;
    private Map<String, List<String>> attendanceData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_report);

        downloadPdfBtn = findViewById(R.id.downloadPdfBtn);
        downloadExcelBtn = findViewById(R.id.downloadExcelBtn);

        regNumber = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", null);
        attendanceRef = FirebaseDatabase.getInstance().getReference("attendance_sessions");

        if (regNumber == null || regNumber.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        fetchAttendanceData();

        downloadPdfBtn.setOnClickListener(v -> generatePdfReport());
        downloadExcelBtn.setOnClickListener(v -> generateExcelReport());
    }

    private void fetchAttendanceData() {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceData.clear();

                for (DataSnapshot sessionSnap : snapshot.getChildren()) {
                    String courseCode = sessionSnap.child("courseCode").getValue(String.class);
                    Long timestamp = sessionSnap.child("timestamp").getValue(Long.class);

                    if (courseCode != null && timestamp != null) {
                        DataSnapshot attendees = sessionSnap.child("attendees");

                        if (attendees.hasChild(regNumber) && attendees.child(regNumber).getValue(Boolean.class)) {
                            String readableTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                                    .format(new Date(timestamp));

                            if (!attendanceData.containsKey(courseCode)) {
                                attendanceData.put(courseCode, new ArrayList<>());
                            }
                            attendanceData.get(courseCode).add(readableTime);
                        }
                    }
                }

                Toast.makeText(DownloadReportActivity.this, "Attendance data loaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DownloadReportActivity.this, "Failed to fetch attendance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generatePdfReport() {
        PdfDocument pdf = new PdfDocument();
        Paint paint = new Paint();
        int y = 50;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(16);
        canvas.drawText("Attendance Report", 220, y, paint);
        y += 30;

        paint.setTextSize(12);

        for (String course : attendanceData.keySet()) {
            canvas.drawText("Course: " + course, 50, y, paint);
            y += 20;

            for (String timestamp : attendanceData.get(course)) {
                canvas.drawText("- " + timestamp, 80, y, paint);
                y += 15;
            }
            y += 20;
        }

        pdf.finishPage(page);

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) downloadsDir.mkdirs();

            File file = new File(downloadsDir, "Attendance_Report.pdf");
            FileOutputStream fos = new FileOutputStream(file);
            pdf.writeTo(fos);
            fos.close();

            Toast.makeText(this, "PDF saved to Downloads:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }

        pdf.close();
    }

    private void generateExcelReport() {
        Workbook workbook = new HSSFWorkbook(); // ✅ Use HSSFWorkbook for .xls
        Sheet sheet = workbook.createSheet("Attendance");

        int rowIndex = 0;
        for (String course : attendanceData.keySet()) {
            Row courseRow = sheet.createRow(rowIndex++);
            courseRow.createCell(0).setCellValue("Course: " + course);

            for (String timestamp : attendanceData.get(course)) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(1).setCellValue(timestamp);
            }

            rowIndex++; // space between courses
        }

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) downloadsDir.mkdirs();

            File file = new File(downloadsDir, "Attendance_Report.xls"); // ✅ Save as .xls
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();

            Toast.makeText(this, "Excel saved to Downloads:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save Excel", Toast.LENGTH_SHORT).show();
        }
    }
}
