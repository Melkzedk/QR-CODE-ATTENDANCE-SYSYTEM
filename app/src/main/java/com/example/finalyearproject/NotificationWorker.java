package com.example.finalyearproject;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("NotificationWorker", "Worker triggered...");

        CountDownLatch latch = new CountDownLatch(1);

        checkForUpcomingClasses(() -> {
            // Schedule next run (every 30 sec for testing only)
            OneTimeWorkRequest nextRun = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInitialDelay(30, TimeUnit.SECONDS)
                    .build();
            WorkManager.getInstance(getApplicationContext()).enqueue(nextRun);

            latch.countDown();
        });

        try {
            latch.await(10, TimeUnit.SECONDS); // Wait max 10 seconds for Firebase callback
        } catch (InterruptedException e) {
            return Result.failure();
        }

        return Result.success();
    }

    private void checkForUpcomingClasses(Runnable onComplete) {
        DatabaseReference timetableRef = FirebaseDatabase.getInstance().getReference("Timetable");
        String currentDay = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());

        timetableRef.child(currentDay).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

                for (DataSnapshot classSnapshot : snapshot.getChildren()) {
                    String time = classSnapshot.child("time").getValue(String.class);
                    String subject = classSnapshot.child("subject").getValue(String.class);

                    if (time != null && subject != null && isClassAboutToStart(time)) {
                        showNotification(subject + " class is about to start!");
                    }
                }

                onComplete.run(); // Notify doWork to continue
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onComplete.run(); // Still count down to prevent hanging
            }
        });
    }

    private boolean isClassAboutToStart(String classTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date now = sdf.parse(new SimpleDateFormat("HH:mm").format(new Date()));
            Date classDate = sdf.parse(classTime);

            long diff = classDate.getTime() - now.getTime();
            return diff > 0 && diff <= 60 * 1000; // within 1 minute
        } catch (Exception e) {
            Log.e("NotificationWorker", "Time parse error: " + e.getMessage());
            return false;
        }
    }

    private void showNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "class_channel")
                .setSmallIcon(R.drawable.outline_circle_notifications_24) // Make sure this icon exists
                .setContentTitle("Class Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(new Random().nextInt(), builder.build());
    }
}
