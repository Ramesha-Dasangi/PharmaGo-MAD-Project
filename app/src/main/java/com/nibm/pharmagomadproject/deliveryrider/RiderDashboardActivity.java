package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.nibm.pharmagomadproject.R;

public class RiderDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_dashboard);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button btnAcceptAssignment = findViewById(R.id.btnAcceptAssignment);
        if (btnAcceptAssignment != null) {
            btnAcceptAssignment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RiderDashboardActivity.this, AssignmentDetailsActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        ConstraintLayout cardInProgress = findViewById(R.id.cardInProgress);
        if (cardInProgress != null) {
            cardInProgress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RiderDashboardActivity.this, DeliveryProgressActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        View navMap = findViewById(R.id.navMap);
        if (navMap != null) {
            navMap.setOnClickListener(v ->
                    startActivity(new Intent(RiderDashboardActivity.this, LiveMapActivity.class)));
        }

        View navHistory = findViewById(R.id.navHistory);
        if (navHistory != null) {
            navHistory.setOnClickListener(v ->
                    startActivity(new Intent(RiderDashboardActivity.this, DeliveryHistoryActivity.class)));
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(RiderDashboardActivity.this, RiderProfileActivity.class)));
        }
    }
}
