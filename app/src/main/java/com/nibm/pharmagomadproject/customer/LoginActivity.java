package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.deliveryrider.RiderDashboardActivity;
import com.nibm.pharmagomadproject.deliveryrider.RiderMainActivity;
import com.nibm.pharmagomadproject.pharmacyowner.DashboardActivity;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        // Log in button
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Create account
        MaterialButton btnCreate = findViewById(R.id.btnCreateAccount);
        btnCreate.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        // Forgot password
        TextView tvForgot = findViewById(R.id.tvForgotPassword);
        tvForgot.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Password reset link sent!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Error: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
            }
        });

        // Admin login
        TextView tvAdmin = findViewById(R.id.tvAdminLogin);
        tvAdmin.setOnClickListener(v ->
                startActivity(new Intent(this, AdminLoginActivity.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String pass  = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            etPassword.setError("Enter your password");
            etPassword.requestFocus();
            return;
        }

        //firebase authentication
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Check the role in firestore
                        String userId = mAuth.getCurrentUser().getUid();
                        checkUserRole(userId);
                    } else {
                        Toast.makeText(this,
                                "Login Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Check the role in firestore and navigate to correct dashboard
    private void checkUserRole(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    String role = doc.exists() && doc.getString("role") != null
                            ? doc.getString("role") : "customer";

                    Intent intent;
                    switch (role) {
                        case "pharmacy_owner":
                            intent = new Intent(this, DashboardActivity.class);
                            break;
                        case "rider":
                            intent = new Intent(this, RiderDashboardActivity.class);
                            break;
                        case "customer":
                        default:
                            intent = new Intent(this, HomeActivity.class);
                            break;
                    }

                    Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // When firestore failed default — customer home
                    Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });
    }
}
