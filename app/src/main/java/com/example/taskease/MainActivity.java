package com.example.taskease;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

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
}