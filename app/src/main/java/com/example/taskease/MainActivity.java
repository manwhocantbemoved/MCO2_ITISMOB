package com.example.taskease;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private boolean isSettled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnToggleStatus = findViewById(R.id.btnToggleStatus);
        btnToggleStatus.setOnClickListener(v -> {
            isSettled = !isSettled;
            updateStatus();
            if (isSettled) {
                Toast.makeText(MainActivity.this, "You've been settled for a while", Toast.LENGTH_SHORT).show();
            }
        });
        updateStatus();

        FloatingActionButton btnCreatetask = findViewById(R.id.fabAdd);
        btnCreatetask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateTaskActivity.class);
                startActivity(intent);
            }
        });

        Button btnStartFocus = findViewById(R.id.btnStartFocus);
        btnStartFocus.setOnClickListener(v-> {
            Intent i = new Intent(MainActivity.this, FocusActivity.class);
            i.putExtra("task_name", "Review Mobile Prototype Flow");
            i.putExtra("task_duration", 25);
            startActivity(i);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_focus)
                startActivity(new Intent(MainActivity.this, FocusActivity.class));
            else if (id == R.id.nav_tasks)
                startActivity(new Intent(MainActivity.this, CreateTaskActivity.class));
            else if (id == R.id.nav_automate)
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            else if (id == R.id.nav_insights)
                startActivity(new Intent(MainActivity.this, InsightsActivity.class));
            else if (id == R.id.nav_home)
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            return true;
        });
    }
    private void updateStatus() {
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvStatusMessage = findViewById(R.id.tvStatusMessage);
        TextView tvWindow = findViewById(R.id.tvWindow);
        Button btnToggleStatus = findViewById(R.id.btnToggleStatus);

        if (isSettled) {
            tvStatus.setText("Settled - 18m");
            tvStatusMessage.setText("You are currently settled, we picked 3 tasks tuned for optimal flow right now.");
            tvWindow.setText("35 mins window open - Next: Sync call at 3:00 PM");
            btnToggleStatus.setText("Simulate: I'm moving");
        } else {
            tvStatus.setText("Moving");
            tvStatusMessage.setText("You are on the move. We'll suggest tasks when you settle.");
            tvWindow.setText("No free window yet");
            btnToggleStatus.setText("Simulate: I'm settled");
        }
    }
}