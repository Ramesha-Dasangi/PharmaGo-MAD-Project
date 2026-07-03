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
import com.nibm.pharmagomadproject.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

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
        tvForgot.setOnClickListener(v ->
                Toast.makeText(this, "Password reset link sent to your email",
                        Toast.LENGTH_SHORT).show());

        // Admin login
        TextView tvAdmin = findViewById(R.id.tvAdminLogin);
        tvAdmin.setOnClickListener(v ->
                startActivity(new Intent(this, AdminLoginActivity.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null
                ? etEmail.getText().toString().trim() : "";
        String pass  = etPassword.getText() != null
                ? etPassword.getText().toString().trim() : "";

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

        // TODO: Firebase Auth sign in
        // FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}