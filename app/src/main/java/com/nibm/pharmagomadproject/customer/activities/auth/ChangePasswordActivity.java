package com.nibm.pharmagomadproject.customer.activities.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();
        if (email == null) {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-authenticate user to verify current password
        AuthCredential credential = EmailAuthProvider.getCredential(email, current);
        MaterialButton btnUpdate = findViewById(R.id.btnUpdatePassword);
        if (btnUpdate != null) {
            btnUpdate.setEnabled(false);
        }

        user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
            if (reauthTask.isSuccessful()) {
                // Password verified. Update password.
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    if (btnUpdate != null) {
                        btnUpdate.setEnabled(true);
                    }
                    if (updateTask.isSuccessful()) {
                        Toast.makeText(ChangePasswordActivity.this,
                                "Password updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this,
                                "Failed to update password: " +
                                        updateTask.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (btnUpdate != null) {
                    btnUpdate.setEnabled(true);
                }
                etCurrentPassword.setError("Incorrect current password");
                etCurrentPassword.requestFocus();
            }
        });
    }
}