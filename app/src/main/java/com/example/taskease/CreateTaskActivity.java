package com.example.taskease;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreateTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_task);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_focus)
                startActivity(new Intent(CreateTaskActivity.this, FocusActivity.class));
            else if (id == R.id.nav_automate)
                startActivity(new Intent(CreateTaskActivity.this, SettingsActivity.class));
            else if (id == R.id.nav_insights)
                startActivity(new Intent(CreateTaskActivity.this, InsightsActivity.class));
            else if (id == R.id.nav_home)
                startActivity(new Intent(CreateTaskActivity.this, MainActivity.class));
            else if (id == R.id.nav_profile) // placeholder for nav_profile
                startActivity(new Intent(CreateTaskActivity.this, ProfileActivity.class));
            return true;
        });

        TextInputEditText durationInput = findViewById(R.id.etDuration);
        setDurationChipListener(R.id.chip15Min, "15", durationInput);
        setDurationChipListener(R.id.chip30Min, "30", durationInput);
        setDurationChipListener(R.id.chip45Min, "45", durationInput);
        setDurationChipListener(R.id.chip60Min, "60", durationInput);

        findViewById(R.id.btnCreateTask).setOnClickListener(v -> validateTask());
    }

    private void setDurationChipListener(int chipId, String duration, TextInputEditText durationInput) {
        Chip chip = findViewById(chipId);
        chip.setOnClickListener(v -> durationInput.setText(duration));
    }

    private void validateTask() {
        TextInputLayout titleLayout = findViewById(R.id.taskTitleLayout);
        TextInputEditText titleInput = findViewById(R.id.etTaskTitle);
        TextInputLayout durationLayout = findViewById(R.id.durationInputLayout);
        TextInputEditText durationInput = findViewById(R.id.etDuration);

        String title = titleInput.getText() == null ? "" : titleInput.getText().toString().trim();
        if (title.isEmpty()) {
            titleLayout.setError("Add a task name");
            titleInput.requestFocus();
            return;
        }
        titleLayout.setError(null);

        String durationText = durationInput.getText() == null ? "" : durationInput.getText().toString().trim();
        int duration;
        try {
            duration = Integer.parseInt(durationText);
        } catch (NumberFormatException e) {
            durationLayout.setError("Enter a duration in minutes");
            durationInput.requestFocus();
            return;
        }

        if (duration <= 0) {
            durationLayout.setError("Duration must be at least 1 minute");
            durationInput.requestFocus();
            return;
        }
        durationLayout.setError(null);

        RadioGroup priorityGroup = findViewById(R.id.priorityGroup);
        RadioGroup energyGroup = findViewById(R.id.energyGroup);
        if (priorityGroup.getCheckedRadioButtonId() == -1 || energyGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Choose a priority and energy level", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}