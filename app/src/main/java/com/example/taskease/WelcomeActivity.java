package com.example.taskease;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.btnRegister).setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class)));

        findViewById(R.id.btnLogin).setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class)));

        // Cancel closes the app
        findViewById(R.id.btnCancel).setOnClickListener(v -> finishAffinity());
    }
}