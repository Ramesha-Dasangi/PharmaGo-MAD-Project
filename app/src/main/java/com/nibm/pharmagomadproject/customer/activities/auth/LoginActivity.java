package com.nibm.pharmagomadproject.customer.activities.auth;

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
import com.nibm.pharmagomadproject.customer.activities.home.HomeActivity;
import com.nibm.pharmagomadproject.deliveryrider.RiderDashboardActivity;
import com.nibm.pharmagomadproject.pharmacyowner.DashboardActivity;
import com.nibm.pharmagomadproject.pharmacyowner.ApprovalSuccessActivity;


public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            btnLogin.setEnabled(false);
            loginUser();
        });

        MaterialButton btnCreate = findViewById(R.id.btnCreateAccount);

        btnCreate.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            this,
                            RegisterActivity.class
                    )
            );
        });

        TextView tvForgot = findViewById(R.id.tvForgotPassword);

        tvForgot.setOnClickListener(v -> {
            String email =
                    etEmail.getText()
                            .toString()
                            .trim();

            if(TextUtils.isEmpty(email)){
                etEmail.setError("Enter email");
                return;
            }




            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(
                                this,
                                "Password reset email sent",
                                Toast.LENGTH_SHORT
                        ).show();
                    })

                    .addOnFailureListener(e -> {
                        Toast.makeText(
                                this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        });

        TextView tvAdmin = findViewById(R.id.tvAdminLogin);

        tvAdmin.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            this,
                            AdminLoginActivity.class
                    )
            );
        });
    }

    private void loginUser(){
        String email = etEmail.getText()
                        .toString()
                        .trim();

        String password = etPassword.getText()
                        .toString()
                        .trim();

        if(TextUtils.isEmpty(email)){
            etEmail.setError("Enter email");
            MaterialButton btnLogin = findViewById(R.id.btnLogin);
            if (btnLogin != null) {
                btnLogin.setEnabled(true);
            }
            return;
        }

        if(TextUtils.isEmpty(password)){
            etPassword.setError("Enter password");
            MaterialButton btnLogin = findViewById(R.id.btnLogin);
            if (btnLogin != null) {
                btnLogin.setEnabled(true);
            }
            return;
        }

        mAuth.signInWithEmailAndPassword(
                        email,
                        password
                )

                .addOnSuccessListener(authResult -> {
                    String uid =
                            mAuth.getCurrentUser()
                                    .getUid();
                    checkUser(uid);
                })


                .addOnFailureListener(e -> {
                    MaterialButton btnLogin = findViewById(R.id.btnLogin);
                    if (btnLogin != null) {
                        btnLogin.setEnabled(true);
                    }
                    Toast.makeText(
                            this,
                            "Login failed: "+e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void checkUser(String uid){
        db.collection("users")
                .document(uid)
                .get()

                .addOnSuccessListener(document -> {
                    if(!document.exists()){
                        Toast.makeText(
                                this,
                                "User data not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        mAuth.signOut();

                        MaterialButton btnLogin = findViewById(R.id.btnLogin);
                        if (btnLogin != null) {
                            btnLogin.setEnabled(true);
                        }
                        return;
                    }

                    String role = document.getString("role");
                    String status = document.getString("status");
                    Boolean approved = document.getBoolean("isApproved");

                    if (approved == null) {
                        approved = false;
                    }

                    if (role == null) {
                        role = "customer";
                    }

                    // Block login if account is blocked
                    if ("blocked".equals(status)) {
                        mAuth.signOut();
                        MaterialButton btnLoginBlocked = findViewById(R.id.btnLogin);
                        if (btnLoginBlocked != null) btnLoginBlocked.setEnabled(true);
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Account Blocked")
                                .setMessage("Your account has been blocked by the admin. Please contact support for assistance.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    switch (role) {
                        case "customer":
                            openCustomer();
                            break;

                        case "pharmacy_owner":
                            if (Boolean.TRUE.equals(approved)) {
                                Boolean approvalSeen = document.getBoolean("approvalSeen");
                                if (approvalSeen != null && approvalSeen) {
                                    openPharmacy();
                                } else {
                                    db.collection("users").document(uid).update("approvalSeen", true);
                                    startActivity(new Intent(LoginActivity.this, ApprovalSuccessActivity.class));
                                    finish();
                                }
                            } else {
                                openPending();
                            }
                            break;

                        case "rider":
                            if (Boolean.TRUE.equals(approved)) {
                                openRider();
                            } else {
                                openPending();
                            }
                            break;

                        case "admin":
                            openAdmin();
                            break;

                        default:
                            Toast.makeText(this, "Invalid account type", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            MaterialButton btnLoginDefault = findViewById(R.id.btnLogin);
                            if (btnLoginDefault != null) btnLoginDefault.setEnabled(true);
                    }
                })

                .addOnFailureListener(e -> {
                    MaterialButton btnLogin = findViewById(R.id.btnLogin);
                    if (btnLogin != null) {
                        btnLogin.setEnabled(true);
                    }
                    Toast.makeText(
                            this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void openCustomer(){

        startActivity(
                new Intent(
                        this,
                        HomeActivity.class
                )
        );
        finish();
    }
    private void openPharmacy(){
        startActivity(
                new Intent(
                        this,
                        DashboardActivity.class
                )
        );
        finish();
    }
    private void openRider(){
        startActivity(
                new Intent(
                        this,
                        RiderDashboardActivity.class
                )
        );
        finish();
    }

    private void openAdmin(){
        startActivity(
                new Intent(
                        this,
                        com.nibm.pharmagomadproject.Admin.AdminDashboardActivity.class
                )
        );
        finish();
    }

    private void openPending(){
        Toast.makeText(
                this,
                "Your account is waiting for admin approval",
                Toast.LENGTH_LONG
        ).show();
        mAuth.signOut();

        startActivity(
                new Intent(
                        this,
                        AccountStatusActivity.class
                )
        );
        finish();
    }
}