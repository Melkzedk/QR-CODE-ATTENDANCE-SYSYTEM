package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class LecturerDashboardActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_dashboard);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Set up toolbar
        setSupportActionBar(toolbar);

        // Set up drawer toggle with toolbar
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_add_course) {
                startActivity(new Intent(this, AddCourseActivity.class));
            } else if (id == R.id.nav_manage_course) {
                startActivity(new Intent(this, ManageCoursesActivity.class));
            } else if (id == R.id.nav_view_timetable) { // ✅ Handle View Timetable
                startActivity(new Intent(this, ViewTimetableActivity.class));
            } else if (id == R.id.nav_mark_attendance) {
                startActivity(new Intent(this, GenerateQRActivity.class));
            } else if (id == R.id.nav_view_attendance) {
                startActivity(new Intent(this, ViewAttendanceActivity.class));
            } else if (id == R.id.nav_generate_report) {
                startActivity(new Intent(this, AttendanceReportActivity.class));
            } else if (id == R.id.nav_send_message) {
                startActivity(new Intent(this, SendMessageActivity.class));
            } else if (id == R.id.nav_view_messages) {
                startActivity(new Intent(this, AnnouncementsActivity.class));
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}
