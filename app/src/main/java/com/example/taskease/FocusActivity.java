package com.example.taskease;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class FocusActivity extends AppCompatActivity {

    private CountDownTimer timer;
    private long timeLeftMillis;
    private boolean running = false;
    private long startedAt = 0;
    private String taskId;
    private int duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_focus);
        TextView tvFocusName = findViewById(R.id.tvFocusName);
        TextView tvFocusDuration = findViewById(R.id.tvFocusDuration);
        taskId = getIntent().getStringExtra("task_id");
        String name = getIntent().getStringExtra("task_name");
        duration = getIntent().getIntExtra("task_duration", 0);
        if (name == null || duration <= 0) {
            name = "Quick Focus";
            duration = 25;
        }
        tvFocusName.setText(name);
        tvFocusDuration.setText(duration + " min");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_focus);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)
                startActivity(new Intent(FocusActivity.this, MainActivity.class));
            else if (id == R.id.nav_automate)
                startActivity(new Intent(FocusActivity.this, SettingsActivity.class));
            else if (id == R.id.nav_insights)
                startActivity(new Intent(FocusActivity.this, InsightsActivity.class));
            else if (id == R.id.nav_profile)
                startActivity(new Intent(FocusActivity.this, ProfileActivity.class));
            return true;
        });
        // this is for the timer and its buttons
        TextView tvTimer = findViewById(R.id.tvTimer);
        Button btnStartPause = findViewById(R.id.btnStartPause);
        timeLeftMillis = duration * 60 * 1000L;
        tvTimer.setText(formatTime(timeLeftMillis));

// this is for the Start / Pause button
        btnStartPause.setOnClickListener(v -> {
            if (running) {
                timer.cancel();
                running = false;
                btnStartPause.setText("Resume");
            } else {
                if (startedAt == 0) startedAt = System.currentTimeMillis();
                timer = new CountDownTimer(timeLeftMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timeLeftMillis = millisUntilFinished;
                        tvTimer.setText(formatTime(millisUntilFinished));
                    }

                    @Override
                    public void onFinish() {
                        running = false;
                        timeLeftMillis = 0;
                        tvTimer.setText("00:00");
                        btnStartPause.setText("Start");
                        Toast.makeText(FocusActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                    }
                }.start();
                running = true;
                btnStartPause.setText("Pause");
            }
        });

        // this is for Mark Completed: save the status and the real time taken, then go back
        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            if (timer != null) timer.cancel();

            int actual = duration;
            if (startedAt != 0) {
                long elapsed = System.currentTimeMillis() - startedAt;
                actual = Math.max(1, (int) Math.ceil(elapsed / 60000.0));
            }

            if (taskId == null) {
                Toast.makeText(this, "Quick Focus done", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            DBManager.getInstance().completeTask(taskId, actual, (ok, msg) -> {
                Toast.makeText(FocusActivity.this, msg, Toast.LENGTH_SHORT).show();
                if (ok) finish();
            });
        });
    }

    // this is for turning milliseconds into text like 24:59
    private String formatTime(long millis) {
        long mins = millis / 60000;
        long secs = (millis / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
    }

    // this is for stopping the timer when the screen closes
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}