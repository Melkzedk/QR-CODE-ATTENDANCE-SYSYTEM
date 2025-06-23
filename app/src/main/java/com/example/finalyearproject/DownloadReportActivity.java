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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class DownloadReportActivity extends AppCompatActivity {

    private Button downloadPdfBtn, downloadExcelBtn;
    private DatabaseReference attendanceRef;
    private String regNumber;
    private Map<String, List<String>> attendanceData = new HashMap<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_report);

        downloadPdfBtn = findViewById(R.id.downloadPdfBtn);
        downloadExcelBtn = findViewById(R.id.downloadExcelBtn);

        regNumber = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", null);

        if (regNumber == null || regNumber.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        attendanceRef = FirebaseDatabase.getInstance().getReference("attendance_sessions");

        downloadPdfBtn.setOnClickListener(v -> {
            fetchAttendanceData(() -> generatePdfReport());
        });

        downloadExcelBtn.setOnClickListener(v -> {
            fetchAttendanceData(() -> generateExcelReport());
        });
    }

    private void fetchAttendanceData(Runnable callback) {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceData.clear();

                for (DataSnapshot session : snapshot.getChildren()) {
                    String courseCode = session.child("courseCode").getValue(String.class);
                    Long timestampLong = session.child("timestamp").getValue(Long.class);

                    if (courseCode != null && timestampLong != null) {
                        String readableTime = sdf.format(new Date(timestampLong));

                        DataSnapshot attendees = session.child("attendees");
                        if (attendees.hasChild(regNumber) && attendees.child(regNumber).getValue(Boolean.class)) {
                            attendanceData
                                    .computeIfAbsent(courseCode, k -> new ArrayList<>())
                                    .add(readableTime);
                        }
                    }
                }

                Toast.makeText(DownloadReportActivity.this, "Attendance loaded", Toast.LENGTH_SHORT).show();
                callback.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DownloadReportActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
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

        for (Map.Entry<String, List<String>> entry : attendanceData.entrySet()) {
            canvas.drawText("Course: " + entry.getKey(), 50, y, paint);
            y += 20;

            for (String time : entry.getValue()) {
                canvas.drawText("- " + time, 80, y, paint);
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
            Toast.makeText(this, "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        pdf.close();
    }

    private void generateExcelReport() {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance");

        int rowIndex = 0;

        for (Map.Entry<String, List<String>> entry : attendanceData.entrySet()) {
            Row courseRow = sheet.createRow(rowIndex++);
            courseRow.createCell(0).setCellValue("Course: " + entry.getKey());

            for (String timestamp : entry.getValue()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(1).setCellValue(timestamp);
            }

            rowIndex++; // space between courses
        }

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) downloadsDir.mkdirs();

            File file = new File(downloadsDir, "Attendance_Report.xls");
            FileOutputStream out = new FileOutputStream(file);
            workbook.write(out);
            out.close();
            workbook.close();

            Toast.makeText(this, "Excel saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Excel save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
