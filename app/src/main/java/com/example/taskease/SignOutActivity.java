package com.example.taskease;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SignOutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_out);

        Button btnConfirm = findViewById(R.id.btn_confirm_sign_out);
        Button btnCancel = findViewById(R.id.btn_cancel);

        btnConfirm.setOnClickListener(v -> {
            Intent intent = new Intent(SignOutActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }
}
