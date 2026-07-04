package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.nibm.pharmagomadproject.Admin.AdminDashboardActivity;
import com.nibm.pharmagomadproject.R;

public class AdminLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText etAdminUsername, etAdminPassword;
    private MaterialButton btnLoginAsAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();

        etAdminUsername = findViewById(R.id.etAdminUsername);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnLoginAsAdmin = findViewById(R.id.btnLoginAsAdmin);

        btnLoginAsAdmin.setOnClickListener(v ->
                attemptAdminLogin());
    }

    private void attemptAdminLogin() {
        String email = etAdminUsername.getText() != null ? etAdminUsername.getText().toString().trim() : "";
        String pass  = etAdminPassword.getText() != null ? etAdminPassword.getText().toString().trim() : "";

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

        // Firebase Authentication login
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Admin login successful", Toast.LENGTH_SHORT).show();
                        // Navigate to Admin Dashboard
                        startActivity(new Intent(this, AdminDashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
