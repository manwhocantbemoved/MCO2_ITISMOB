package com.example.taskease;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView tvUserName = findViewById(R.id.tv_user_name);
        TextView tvUserEmail = findViewById(R.id.tv_user_email);
        Button btnSync = findViewById(R.id.btn_sync_data);
        Button btnSignOut = findViewById(R.id.btn_sign_out);
        Slider sliderSensitivity = findViewById(R.id.slider_sensitivity);
        MaterialSwitch switchQuiet = findViewById(R.id.switch_quiet_hours);
        LinearLayout layoutTimePickers = findViewById(R.id.layout_time_pickers);
        Button btnStart = findViewById(R.id.btn_start_time);
        Button btnEnd = findViewById(R.id.btn_end_time);
        Button btnBlacklist = findViewById(R.id.btn_manage_blacklist);

        // Intent: Sign Out screen
        btnSignOut.setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, SignOutActivity.class)));

        // Intent: Sign Up screen (tap the account name/email to register or switch account)
        tvUserName.setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, SignUpActivity.class)));

        // Show/hide quiet hour pickers
        switchQuiet.setOnCheckedChangeListener((btn, checked) ->
                layoutTimePickers.setVisibility(checked ? View.VISIBLE : View.GONE));

        // TODO: wire these to your SQLite / SharedPreferences logic
        // sliderSensitivity.addOnChangeListener((s, value, fromUser) -> { });
        // btnStart / btnEnd -> TimePickerDialog
        // btnSync -> back up local DB
        // btnBlacklist -> startActivity(new Intent(this, BlacklistActivity.class));
    }
}
