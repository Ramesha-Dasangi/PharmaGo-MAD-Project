package com.nibm.pharmagomadproject.customer.activities.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nibm.pharmagomadproject.R;

public class ChangePasswordActivity extends AppCompatActivity {
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword        = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(
                v ->
                        finish()
        );

        // Update password button
        MaterialButton btnUpdate = findViewById(R.id.btnUpdatePassword);
        btnUpdate.setOnClickListener(
                v ->
                        updatePassword()
        );
    }

    private void updatePassword() {
        String current = etCurrentPassword.getText() != null
                ? etCurrentPassword.getText().toString().trim() : "";
        String newPass  = etNewPassword.getText() != null
                ? etNewPassword.getText().toString().trim() : "";
        String confirm  = etConfirmNewPassword.getText() != null
                ? etConfirmNewPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(current)) {
            etCurrentPassword.setError("Enter your current password");
            etCurrentPassword.requestFocus();
            return;
        }
        if (newPass.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }
        if (!newPass.equals(confirm)) {
            etConfirmNewPassword.setError("Passwords do not match");
            etConfirmNewPassword.requestFocus();
            return;
        }

        // TODO: verify current password with Firebase Auth, then update
        Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
        finish();

    }
}