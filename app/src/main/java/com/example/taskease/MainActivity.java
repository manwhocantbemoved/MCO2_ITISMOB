package com.example.taskease;

import android.app.AlertDialog;
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
                showSettledDialog();
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

        Button btnStartFocus2 = findViewById(R.id.btnStartFocus2);
        btnStartFocus2.setOnClickListener(v->{
            Intent i = new Intent(MainActivity.this, FocusActivity.class);
            i.putExtra("task_name", "Workout");
            i.putExtra("task_duration", 45);
            startActivity(i);
        });

        Button btnStartFocus3 = findViewById(R.id.btnStartFocus3);
        btnStartFocus3.setOnClickListener(v->{
            Intent i = new Intent(MainActivity.this, FocusActivity.class);
            i.putExtra("task_name", "Study");
            i.putExtra("task_duration", 30);
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

        Button btnFilterAll = findViewById(R.id.btnFilterAll);
        Button btnFilterShort = findViewById(R.id.btnFilterShort);
        Button btnFilterHigh = findViewById(R.id.btnFilterHigh);

        btnFilterAll.setOnClickListener(v -> applyFilter("all"));
        btnFilterShort.setOnClickListener(v -> applyFilter("short"));
        btnFilterHigh.setOnClickListener(v -> applyFilter("high"));
    }

    private void applyFilter(String filter){
        View card1 = findViewById(R.id.taskCard);
        View card2 = findViewById(R.id.taskCard2);
        View card3 = findViewById(R.id.taskCard3);

        card1.setVisibility(View.VISIBLE);
        card2.setVisibility(View.VISIBLE);
        card3.setVisibility(View.VISIBLE);

        if (filter.equals("short")) {
            card2.setVisibility(View.GONE);
        } else if (filter.equals("high")) {
            card2.setVisibility(View.GONE);
            card3.setVisibility(View.GONE);
        }
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

    private void showSettledDialog(){
        String[] tasks = {
                "Review Mobile Prototype Flow (25 min)",
                "Workout (45 min)",
                "Study (30 min)"
        };

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("You're settled! 35 mins free")
                .setItems(tasks, null)
                .setNegativeButton("Dismiss", null)
                .show();
    }
}