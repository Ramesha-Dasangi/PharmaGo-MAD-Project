package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.nibm.pharmagomadproject.R;

public class AssignmentDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_details);
        getSupportActionBar().hide();

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        Button btnStartNavigation = findViewById(R.id.btnStartNavigation);
        if (btnStartNavigation != null) {
            btnStartNavigation.setOnClickListener(v ->
                    startActivity(new Intent(AssignmentDetailsActivity.this, PickupNavigationActivity.class)));
        }

        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent i = new Intent(this, RiderDashboardActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }
        View navMap = findViewById(R.id.navMap);
        if (navMap != null) navMap.setOnClickListener(v ->
                startActivity(new Intent(this, LiveMapActivity.class)));

        View navHistory = findViewById(R.id.navHistory);
        if (navHistory != null) navHistory.setOnClickListener(v ->
                startActivity(new Intent(this, DeliveryHistoryActivity.class)));

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, RiderProfileActivity.class)));
    }
}
