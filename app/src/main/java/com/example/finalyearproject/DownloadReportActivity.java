// Replace your entire DownloadReportActivity.java content with this:

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class DownloadReportActivity extends AppCompatActivity {

    private Button downloadPdfBtn, downloadExcelBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference attendanceRef;
    private String studentId;
    private Map<String, List<String>> attendanceData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_report);

        downloadPdfBtn = findViewById(R.id.downloadPdfBtn);
        downloadExcelBtn = findViewById(R.id.downloadExcelBtn);

        mAuth = FirebaseAuth.getInstance();
        studentId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        attendanceRef = FirebaseDatabase.getInstance().getReference("Attendance");

        if (studentId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
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
                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    String courseCode = courseSnap.getKey();
                    List<String> timestamps = new ArrayList<>();

                    for (DataSnapshot dateSnap : courseSnap.getChildren()) {
                        if (dateSnap.child(studentId).getValue(Boolean.class) != null) {
                            timestamps.add(dateSnap.getKey());
                        }
                    }

                    if (!timestamps.isEmpty()) {
                        attendanceData.put(courseCode, timestamps);
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
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            File file = new File(downloadsDir, "Attendance_Report.pdf");
            FileOutputStream fos = new FileOutputStream(file);
            pdf.writeTo(fos);
            fos.close();
            Toast.makeText(this, "PDF saved to Downloads: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }

        pdf.close();
    }

    private void generateExcelReport() {
        Workbook workbook = new XSSFWorkbook();
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
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            File file = new File(downloadsDir, "Attendance_Report.xlsx");
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();
            workbook.close();
            Toast.makeText(this, "Excel saved to Downloads: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save Excel", Toast.LENGTH_SHORT).show();
        }
    }
}
