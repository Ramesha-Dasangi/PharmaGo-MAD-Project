package com.nibm.pharmagomadproject.pharmacyowner.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nibm.pharmagomadproject.R;

import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etCurrentPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private MaterialButton btnUpdatePassword;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword     = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmNewPassword); // Note: correct layout ID is etConfirmNewPassword
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Update Password
        btnUpdatePassword.setOnClickListener(v -> {

            String currentPassword = etCurrentPassword.getText() != null ? etCurrentPassword.getText().toString().trim() : "";
            String newPassword     = etNewPassword.getText() != null ? etNewPassword.getText().toString().trim() : "";
            String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

            // Validation
            if (TextUtils.isEmpty(currentPassword)) {
                etCurrentPassword.setError("Current password is required");
                etCurrentPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(newPassword)) {
                etNewPassword.setError("New password is required");
                etNewPassword.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                etConfirmPassword.setError("Confirm your password");
                etConfirmPassword.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            // Password strength validation
            if (!isValidPassword(newPassword)) {
                etNewPassword.setError(
                        "Minimum 8 characters, 1 uppercase, 1 lowercase and 1 number"
                );
                etNewPassword.requestFocus();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String email = user.getEmail();
            if (email == null) {
                Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
                return;
            }

            btnUpdatePassword.setEnabled(false);

            // Re-authenticate user to verify current password
            AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    // Re-authentication successful, update password in Firebase Auth
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        btnUpdatePassword.setEnabled(true);
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(
                                    ChangePasswordActivity.this,
                                    "Password Updated Successfully",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        } else {
                            Toast.makeText(
                                    ChangePasswordActivity.this,
                                    "Failed to update password: " + updateTask.getException().getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                } else {
                    btnUpdatePassword.setEnabled(true);
                    etCurrentPassword.setError("Incorrect current password");
                    etCurrentPassword.requestFocus();
                }
            });
        });
    }

    // Password Validation (8+ chars, 1 uppercase, 1 lowercase, 1 digit)
    private boolean isValidPassword(String password) {
        Pattern PASSWORD_PATTERN = Pattern.compile(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{8,}$"
        );
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}