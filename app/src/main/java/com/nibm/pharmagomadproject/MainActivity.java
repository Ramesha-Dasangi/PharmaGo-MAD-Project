package com.nibm.pharmagomadproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.customer.activities.home.HomeActivity;
import com.nibm.pharmagomadproject.customer.activities.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            com.google.firebase.auth.FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                String role = document.getString("role");
                                Boolean approved = document.getBoolean("isApproved");
                                if (approved == null) approved = false;
                                if (role == null) role = "customer";

                                Intent intent;
                                switch (role) {
                                    case "customer":
                                        intent = new Intent(MainActivity.this, HomeActivity.class);
                                        break;
                                    case "pharmacy_owner":
                                        if (Boolean.TRUE.equals(approved)) {
                                            Boolean approvalSeen = document.getBoolean("approvalSeen");
                                            if (approvalSeen != null && approvalSeen) {
                                                intent = new Intent(MainActivity.this, com.nibm.pharmagomadproject.pharmacyowner.DashboardActivity.class);
                                            } else {
                                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                        .collection("users").document(uid).update("approvalSeen", true);
                                                intent = new Intent(MainActivity.this, com.nibm.pharmagomadproject.pharmacyowner.ApprovalSuccessActivity.class);
                                            }
                                        } else {
                                            intent = new Intent(MainActivity.this, com.nibm.pharmagomadproject.customer.activities.auth.AccountStatusActivity.class);
                                        }
                                        break;
                                    case "rider":
                                        if (Boolean.TRUE.equals(approved)) {
                                            intent = new Intent(MainActivity.this, com.nibm.pharmagomadproject.deliveryrider.RiderDashboardActivity.class);
                                        } else {
                                            intent = new Intent(MainActivity.this, com.nibm.pharmagomadproject.customer.activities.auth.AccountStatusActivity.class);
                                        }
                                        break;
                                    default:
                                        intent = new Intent(MainActivity.this, LoginActivity.class);
                                }
                                startActivity(intent);
                                finish();
                            } else {
                                mAuth.signOut();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Offline or network error: fall back to LoginActivity
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        });
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }, 1800); // 1.8second splash delay
    }
}