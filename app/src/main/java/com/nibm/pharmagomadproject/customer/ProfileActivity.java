package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;

import com.nibm.pharmagomadproject.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_profile);
        getSupportActionBar().hide();

        // Row: Delivery address
        LinearLayout rowAddress = findViewById(R.id.rowAddress);
        rowAddress.setOnClickListener(v -> {
            startActivity(new Intent(this, DeliveryAddressActivity.class));
        });

        // Row: Order history
        LinearLayout rowOrderHistory = findViewById(R.id.rowOrderHistory);
        rowOrderHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, OrderHistoryActivity.class));
        });

        // Row: Change password
        LinearLayout rowChangePassword = findViewById(R.id.rowChangePassword);
        rowChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        // Row: Log out
        LinearLayout rowLogout = findViewById(R.id.rowLogout);
        rowLogout.setOnClickListener(v -> showLogoutDialog());

        // Bottom nav
        setupBottomNav();
    }

    private void showLogoutDialog() {
        android.view.View dialogView = getLayoutInflater()
                .inflate(R.layout.activity_dialog_logout, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnCancelLogout)
                .setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnConfirmLogout)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    // Clear session and go to login
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });

        dialog.show();
    }

    private void setupBottomNav() {
        // navProfile is already active (no tint change needed)
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.navSearch).setOnClickListener(v -> {
            // TODO: SearchActivity
        });
        findViewById(R.id.navCart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
            finish();
        });
        findViewById(R.id.navOrders).setOnClickListener(v -> {
            startActivity(new Intent(this, OrderHistoryActivity.class));
            finish();
        });
    }
}
