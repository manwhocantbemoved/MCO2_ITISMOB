package com.example.taskease;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean isSettled = false;
    private TaskAdapter adapter;
    private List<TaskItem> allTasks = new ArrayList<>();
    private String currentFilter = "all";
    private static final int FREE_WINDOW_MIN = 35;

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

        // Simulate button
        Button btnToggleStatus = findViewById(R.id.btnToggleStatus);
        btnToggleStatus.setOnClickListener(v -> {
            isSettled = !isSettled;
            updateStatus();
            if (isSettled) {
                showSettledDialog();
            }
        });
        updateStatus();

        // + button opens Create Task
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CreateTaskActivity.class)));

        // Task list from the database
        RecyclerView rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapter = new TaskAdapter(task -> {
            Intent i = new Intent(MainActivity.this, FocusActivity.class);
            i.putExtra("task_name", task.name);
            i.putExtra("task_duration", task.duration);
            startActivity(i);
        });
        rvTasks.setAdapter(adapter);

        DBManager.getInstance().listenTasks(tasks -> {
            allTasks = tasks;
            refreshList();
        });

        // Filter buttons
        findViewById(R.id.btnFilterAll).setOnClickListener(v -> {
            currentFilter = "all";
            refreshList();
        });
        findViewById(R.id.btnFilterShort).setOnClickListener(v -> {
            currentFilter = "short";
            refreshList();
        });
        findViewById(R.id.btnFilterHigh).setOnClickListener(v -> {
            currentFilter = "high";
            refreshList();
        });

        // Bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_focus)
                startActivity(new Intent(MainActivity.this, FocusActivity.class));
            else if (id == R.id.nav_profile)
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            else if (id == R.id.nav_automate)
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            else if (id == R.id.nav_insights)
                startActivity(new Intent(MainActivity.this, InsightsActivity.class));
            return true;
        });
    }

    private List<TaskItem> getFittingTasks() {
        List<TaskItem> fitting = new ArrayList<>();
        for (TaskItem t : allTasks) {
            if ("completed".equals(t.status))
                continue;
            if (t.duration <= FREE_WINDOW_MIN)
                fitting.add(t);
        }
        return fitting;
    }

    private void refreshList() {
        List<TaskItem> shown = new ArrayList<>();
        for (TaskItem t : allTasks) {
            if ("completed".equals(t.status)) continue;
            if (currentFilter.equals("short") && t.duration > 30) continue;
            if (currentFilter.equals("high") && !"High".equals(t.priority)) continue;
            shown.add(t);
        }
        adapter.setTasks(shown);
    }

    private void updateStatus() {
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvStatusMessage = findViewById(R.id.tvStatusMessage);
        TextView tvWindow = findViewById(R.id.tvWindow);
        Button btnToggleStatus = findViewById(R.id.btnToggleStatus);

        if (isSettled) {
            tvStatus.setText("Settled - 18m");
            tvStatusMessage.setText("You are currently settled. We found "
                    + getFittingTasks().size() + " tasks that fit your window.");
            tvWindow.setText(FREE_WINDOW_MIN + " mins window open");
            btnToggleStatus.setText("Simulate: I'm moving");
        } else {
            tvStatus.setText("Moving");
            tvStatusMessage.setText("You are on the move. We'll suggest tasks when you settle.");
            tvWindow.setText("No free window yet");
            btnToggleStatus.setText("Simulate: I'm settled");
        }
    }

    private void showSettledDialog() {
        List<TaskItem> fitting = getFittingTasks();

        if (fitting.isEmpty()) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("You're settled! " + FREE_WINDOW_MIN + " mins free")
                    .setMessage("No saved tasks fit this window. Tap + to add one.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String[] labels = new String[fitting.size()];
        for (int i = 0; i < fitting.size(); i++) {
            TaskItem t = fitting.get(i);
            labels[i] = t.name + " (" + t.duration + " min, " + t.priority + ")";
        }

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("You're settled! " + FREE_WINDOW_MIN + " mins free")
                .setItems(labels, (dialog, which) -> {
                    TaskItem picked = fitting.get(which);
                    Intent i = new Intent(MainActivity.this, FocusActivity.class);
                    i.putExtra("task_name", picked.name);
                    i.putExtra("task_duration", picked.duration);
                    startActivity(i);
                })
                .setNegativeButton("Dismiss", null)
                .show();
    }
}