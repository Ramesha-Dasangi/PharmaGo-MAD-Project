package com.nibm.pharmagomadproject.pharmacyowner.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.pharmacyowner.DashboardActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageView btnBack;

    private TextInputEditText etEmail;

    private Button  btnSendCode;
    private Button btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Views

        btnBack = findViewById(R.id.btnBack);

        etEmail = findViewById(R.id.etEmail);

        btnSendCode = findViewById(R.id.btnSendCode);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        // Back Arrow

        btnBack.setOnClickListener(v -> finish());

        // Send Code Button

        btnSendCode.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();

            // Empty Validation

            if (email.isEmpty()) {

                etEmail.setError("Please enter your email");
                etEmail.requestFocus();
                return;

            }

            // Email Validation

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                etEmail.setError("Enter a valid email address");
                etEmail.requestFocus();
                return;

            }

            // Dummy Success Message

            Toast.makeText(
                    ForgotPasswordActivity.this,
                    "Verification code sent successfully!",
                    Toast.LENGTH_LONG
            ).show();

            // Firebase code will be added later

        });

        // Back to Login

        btnBackToLogin.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ForgotPasswordActivity.this,
                    DashboardActivity.class
            );

            startActivity(intent);
            finish();

        });

    }

}