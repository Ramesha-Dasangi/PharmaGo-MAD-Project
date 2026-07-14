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
import com.nibm.pharmagomadproject.Admin.AdminDashboardActivity;
import com.nibm.pharmagomadproject.R;

import java.util.HashMap;
import java.util.Map;

public class AdminLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etAdminUsername, etAdminPassword;
    private MaterialButton btnLoginAsAdmin;
    private TextView tvBackToCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_login);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        etAdminUsername  = findViewById(R.id.etAdminUsername);
        etAdminPassword  = findViewById(R.id.etAdminPassword);
        btnLoginAsAdmin  = findViewById(R.id.btnLoginAsAdmin);
        tvBackToCustomer = findViewById(R.id.tvBackToCustomerLogin);

        btnLoginAsAdmin.setOnClickListener(
                v -> attemptAdminLogin()
        );
        tvBackToCustomer.setOnClickListener(v -> finish());
    }

    private void attemptAdminLogin() {
        String email = etAdminUsername.getText() != null
                ? etAdminUsername.getText().toString().trim() : "";
        String pass  = etAdminPassword.getText() != null
                ? etAdminPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etAdminUsername.setError("Enter admin email");
            etAdminUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            etAdminPassword.setError("Enter password");
            etAdminPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        verifyAdminRole(userId);
                    } else {
                        Toast.makeText(this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyAdminRole(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {

                    // First time admin doc create
                    if (!doc.exists()) {
                        Map<String, Object> adminData = new HashMap<>();
                        adminData.put("name",       "Admin");
                        adminData.put("email",      mAuth.getCurrentUser().getEmail());
                        adminData.put("role",       "admin");
                        adminData.put("isApproved", true);

                        db.collection("users").document(userId)
                                .set(adminData)
                                .addOnSuccessListener(v -> goToAdminDashboard())
                                .addOnFailureListener(e -> {
                                    mAuth.signOut();
                                    Toast.makeText(this,
                                            "Setup failed: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                        return;
                    }

                    // If document have,check the role
                    String role = doc.getString("role");
                    if ("admin".equals(role)) {
                        goToAdminDashboard();
                    } else {
                        mAuth.signOut();
                        Toast.makeText(this,
                                "Access denied. Not an admin account.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    mAuth.signOut();
                    Toast.makeText(this,
                            "Verification failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void goToAdminDashboard() {
        Toast.makeText(this, "Welcome, Admin!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, AdminDashboardActivity.class));
        finish();
    }
}