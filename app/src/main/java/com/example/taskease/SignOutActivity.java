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
            // TODO: clear the saved session (SharedPreferences, etc.)
            // Intent: back to Sign Up and clear the back stack
            Intent intent = new Intent(SignOutActivity.this, SignUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Intent: back to Settings
        btnCancel.setOnClickListener(v -> finish());
    }
}
