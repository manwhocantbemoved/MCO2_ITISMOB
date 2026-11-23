package com.example.taskease;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    // this is for reading and saving the settings (the Dashboard reads the same keys)
    private SharedPreferences prefs;
    private Button btnQuietStart, btnQuietEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // this is for keeping the screen clear of the status bar and navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("taskease_settings", MODE_PRIVATE);

        // this is for the Sensitivity slider (5 to 45 minutes)
        SeekBar seek = findViewById(R.id.seekSensitivity);
        TextView tvSensitivity = findViewById(R.id.tvSensitivityValue);
        int savedMin = prefs.getInt("sensitivity_min", 15);
        seek.setProgress(savedMin - 5);
        tvSensitivity.setText(savedMin + " minutes");
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                int minutes = progress + 5;
                tvSensitivity.setText(minutes + " minutes");
                prefs.edit().putInt("sensitivity_min", minutes).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });

        // this is for the Quiet Hours on/off switch
        SwitchCompat switchQuiet = findViewById(R.id.switchQuiet);
        switchQuiet.setChecked(prefs.getBoolean("quiet_enabled", false));
        switchQuiet.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean("quiet_enabled", checked).apply());

        // this is for the Quiet Hours start and end time buttons
        btnQuietStart = findViewById(R.id.btnQuietStart);
        btnQuietEnd = findViewById(R.id.btnQuietEnd);
        showQuietTimes();
        btnQuietStart.setOnClickListener(v -> pickTime("quiet_start_min", 22 * 60));
        btnQuietEnd.setOnClickListener(v -> pickTime("quiet_end_min", 7 * 60));

        // this is for the Location Blacklist list and its two buttons
        showBlacklist();
        findViewById(R.id.btnAddBlacklist).setOnClickListener(v -> addCurrentLocationToBlacklist());
        findViewById(R.id.btnClearBlacklist).setOnClickListener(v -> {
            prefs.edit().remove("blacklist_places").apply();
            showBlacklist();
        });

        // this is for the Sign Out button
        findViewById(R.id.btnSignOut).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, SignOutActivity.class)));

        // this is for the bottom navigation bar
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_automate);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            else if (id == R.id.nav_focus)
                startActivity(new Intent(SettingsActivity.this, FocusActivity.class));
            else if (id == R.id.nav_insights)
                startActivity(new Intent(SettingsActivity.this, InsightsActivity.class));
            else if (id == R.id.nav_profile)
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
            return true;
        });
    }

    // this is for opening a time picker and saving the chosen time as minutes after midnight
    private void pickTime(String key, int defaultMinutes) {
        int current = prefs.getInt(key, defaultMinutes);
        new TimePickerDialog(this, (view, hour, minute) -> {
            prefs.edit().putInt(key, hour * 60 + minute).apply();
            showQuietTimes();
        }, current / 60, current % 60, false).show();
    }

    // this is for showing the saved Quiet Hours times on the two buttons
    private void showQuietTimes() {
        btnQuietStart.setText("From: " + formatTime(prefs.getInt("quiet_start_min", 22 * 60)));
        btnQuietEnd.setText("Until: " + formatTime(prefs.getInt("quiet_end_min", 7 * 60)));
    }

    // this is for turning minutes after midnight into text like 10:00 PM
    private String formatTime(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        String amPm = h >= 12 ? "PM" : "AM";
        int h12 = (h % 12 == 0) ? 12 : h % 12;
        return String.format(Locale.getDefault(), "%d:%02d %s", h12, m, amPm);
    }

    // this is for showing the saved blacklisted places on the screen
    private void showBlacklist() {
        TextView tv = findViewById(R.id.tvBlacklist);
        Set<String> places = prefs.getStringSet("blacklist_places", new HashSet<>());
        if (places.isEmpty()) {
            tv.setText("No places added yet");
            return;
        }
        StringBuilder sb = new StringBuilder();
        int n = 1;
        for (String p : places) {
            sb.append("Place ").append(n++).append(": ").append(p).append("\n");
        }
        tv.setText(sb.toString().trim());
    }

    // this is for saving the user's current location as a blacklisted place
    @SuppressLint("MissingPermission")
    private void addCurrentLocationToBlacklist() {
        boolean fine = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!fine && !coarse) {
            Toast.makeText(this, "Allow location first (open the Dashboard)", Toast.LENGTH_SHORT).show();
            return;
        }

        LocationServices.getFusedLocationProviderClient(this)
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(loc -> {
                    if (loc == null) {
                        Toast.makeText(this, "Couldn't get location. Set one in Extended controls.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // copy the set first, because Android doesn't allow editing the saved one directly
                    Set<String> places = new HashSet<>(prefs.getStringSet("blacklist_places", new HashSet<>()));
                    places.add(loc.getLatitude() + "," + loc.getLongitude());
                    prefs.edit().putStringSet("blacklist_places", places).apply();
                    showBlacklist();
                });
    }
}