//Splash Activity

package com.example.finalyearproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView progressText;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.splashProgressBar);
        progressText = findViewById(R.id.progressPercentage);

        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1;
                handler.post(() -> {
                    progressBar.setProgress(progressStatus);
                    progressText.setText(progressStatus + " %");
                });
                try {
                    Thread.sleep(30); // adjust speed of progress
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // When done, launch RegisterActivity
            startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
            finish();
        }).start();
    }
}
