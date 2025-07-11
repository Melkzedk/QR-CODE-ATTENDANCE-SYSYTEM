//Download Report Activity
package com.example.finalyearproject;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class DownloadReportActivity extends AppCompatActivity {

    private Button downloadPdfBtn, downloadExcelBtn;
    private DatabaseReference attendanceRef;
    private String regNumber;
    private List<Map<String, String>> attendanceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_report);

        downloadPdfBtn = findViewById(R.id.downloadPdfBtn);
        downloadExcelBtn = findViewById(R.id.downloadExcelBtn);

        // Load regNumber from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        regNumber = prefs.getString("regNumber", null);

        if (regNumber == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        attendanceRef = FirebaseDatabase.getInstance().getReference("attendance_sessions");

        fetchAttendanceData();

        downloadPdfBtn.setOnClickListener(v -> generatePdf());
        downloadExcelBtn.setOnClickListener(v -> generateExcel());
    }

    private void fetchAttendanceData() {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                attendanceList.clear();
                for (DataSnapshot sessionSnap : snapshot.getChildren()) {
                    String courseCode = sessionSnap.child("courseCode").getValue(String.class);
                    Long timestampLong = sessionSnap.child("timestamp").getValue(Long.class);
                    DataSnapshot attendees = sessionSnap.child("attendees");

                    if (courseCode != null && timestampLong != null && attendees.hasChild(regNumber)) {
                        String readableDate = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                .format(new Date(timestampLong));

                        Map<String, String> record = new HashMap<>();
                        record.put("courseCode", courseCode);
                        record.put("timestamp", readableDate);
                        attendanceList.add(record);
                    }
                }

                if (attendanceList.isEmpty()) {
                    Toast.makeText(DownloadReportActivity.this, "No attendance records found.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DownloadReportActivity.this, "Attendance data loaded.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DownloadReportActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generatePdf() {
        if (attendanceList.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdf = new PdfDocument();
        Paint paint = new Paint();
        int y = 50;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(16);
        canvas.drawText("Attendance Report", 200, y, paint);
        y += 30;

        paint.setTextSize(12);
        for (Map<String, String> record : attendanceList) {
            canvas.drawText("Course: " + record.get("courseCode") + " - " + record.get("timestamp"), 50, y, paint);
            y += 20;
        }

        pdf.finishPage(page);

        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "Attendance_Report.pdf");

            FileOutputStream fos = new FileOutputStream(file);
            pdf.writeTo(fos);
            fos.close();

            Toast.makeText(this, "PDF saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "PDF error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        pdf.close();
    }

    private void generateExcel() {
        if (attendanceList.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Course Code");
        header.createCell(1).setCellValue("Date");

        int rowIndex = 1;
        for (Map<String, String> record : attendanceList) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(record.get("courseCode"));
            row.createCell(1).setCellValue(record.get("timestamp"));
        }

        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "Attendance_Report.xls");

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            Toast.makeText(this, "Excel saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Excel error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
