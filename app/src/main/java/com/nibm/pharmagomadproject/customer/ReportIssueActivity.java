package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nibm.pharmagomadproject.R;

public class ReportIssueActivity extends AppCompatActivity {

    private String selectedTarget = "pharmacy";

    private MaterialCardView optionPharmacy, optionRider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_issue);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        optionPharmacy = findViewById(R.id.optionPharmacy);
        optionRider = findViewById(R.id.optionRider);

        MaterialButton btnContinue = findViewById(R.id.btnContinue);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // default selection
        selectTarget("pharmacy");

        optionPharmacy.setOnClickListener(v -> selectTarget("pharmacy"));
        optionRider.setOnClickListener(v -> selectTarget("rider"));

        btnContinue.setOnClickListener(v -> {

            Intent intent;

            if ("pharmacy".equals(selectedTarget)) {
                Log.d("ReportIssue", "Opening ComplaintPharmacyActivity");
                intent = new Intent(ReportIssueActivity.this, ComplaintPharmacyActivity.class);
            } else {
                Log.d("ReportIssue", "Opening ComplaintRiderActivity");
                intent = new Intent(ReportIssueActivity.this, ComplaintRiderActivity.class);
            }

            startActivity(intent);
        });
    }

    private void selectTarget(String target) {
        selectedTarget = target;

        int selectedBg = getResources().getColor(R.color.pg_primary_light, null);
        int normalBg = getResources().getColor(R.color.pg_card, null);

        optionPharmacy.setCardBackgroundColor(
                "pharmacy".equals(target) ? selectedBg : normalBg
        );

        optionRider.setCardBackgroundColor(
                "rider".equals(target) ? selectedBg : normalBg
        );
    }
}