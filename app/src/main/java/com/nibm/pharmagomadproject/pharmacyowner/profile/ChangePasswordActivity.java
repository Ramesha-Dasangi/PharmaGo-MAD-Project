package com.nibm.pharmagomadproject.pharmacyowner.profile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nibm.pharmagomadproject.R;

import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnUpdatePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize Views

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Update Password

        btnUpdatePassword.setOnClickListener(v -> {

            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Current Password

            if (currentPassword.isEmpty()) {

                etCurrentPassword.setError("Current password is required");
                etCurrentPassword.requestFocus();
                return;

            }

            // New Password

            if (newPassword.isEmpty()) {

                etNewPassword.setError("New password is required");
                etNewPassword.requestFocus();
                return;

            }

            // Confirm Password

            if (confirmPassword.isEmpty()) {

                etConfirmPassword.setError("Confirm your password");
                etConfirmPassword.requestFocus();
                return;

            }

            // Password Match

            if (!newPassword.equals(confirmPassword)) {

                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;

            }

            // Password Strength

            if (!isValidPassword(newPassword)) {

                etNewPassword.setError(
                        "Minimum 8 characters, 1 uppercase, 1 lowercase and 1 number"
                );

                etNewPassword.requestFocus();
                return;

            }

            // Success Message (Firebase later)

            Toast.makeText(
                    ChangePasswordActivity.this,
                    "Password Updated Successfully",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

        });

    }

    // Password Validation

    private boolean isValidPassword(String password) {

        Pattern PASSWORD_PATTERN = Pattern.compile(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{8,}$"
        );

        return PASSWORD_PATTERN.matcher(password).matches();

    }

}