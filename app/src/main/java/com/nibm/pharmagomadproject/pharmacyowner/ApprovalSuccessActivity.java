package com.nibm.pharmagomadproject.pharmacyowner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;

public class ApprovalSuccessActivity extends AppCompatActivity {

    private Button btnDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval_success);

        // Initialize Button
        btnDashboard = findViewById(R.id.btnDashboard);

        // Go to Dashboard
        btnDashboard.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ApprovalSuccessActivity.this,
                    DashboardActivity.class
            );

            // Clear previous activities
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();

        });

    }

    @Override
    public void onBackPressed() {
        // Prevent going back to approval screen
        finishAffinity();
    }
}