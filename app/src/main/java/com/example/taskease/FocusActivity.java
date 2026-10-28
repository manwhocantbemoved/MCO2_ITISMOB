package com.example.taskease;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FocusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_focus);
        TextView tvFocusName = findViewById(R.id.tvFocusName);
        TextView tvFocusDuration = findViewById(R.id.tvFocusDuration);
        String name = getIntent().getStringExtra("task_name");
        int duration = getIntent().getIntExtra("task_duration", 0);
        if (name == null) {
            // set tvFocusName to "Quick Focus" PLACEHOLDER ONLI
            tvFocusName.setText("Quick Focus");
            // set tvFocusDuration to "25 min" PLACEHOLDER ONLI
            tvFocusDuration.setText("25 min");
        } else {
            // set tvFocusName to name PLACEHOLDER ONLI
            tvFocusName.setText(name);
            // set tvFocusDuration to duration + " min" PLACEHOLDER ONLI
            tvFocusDuration.setText(duration + " min");
        }
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
        TextView tvTimer = findViewById(R.id.tvTimer);
        int minutes = (duration > 0) ? duration : 25;
        tvTimer.setText(minutes + ":00");

        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            Toast.makeText(FocusActivity.this, "Task completed", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(FocusActivity.this, MainActivity.class));
        });
    }
}