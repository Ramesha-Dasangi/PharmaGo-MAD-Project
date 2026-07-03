package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.os.Bundle;

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

        // ✅ MaterialCardView import fix — crash resolved
        optionPharmacy = findViewById(R.id.optionPharmacy);
        optionRider    = findViewById(R.id.optionRider);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        selectTarget("pharmacy");

        optionPharmacy.setOnClickListener(v -> selectTarget("pharmacy"));
        optionRider.setOnClickListener(v    -> selectTarget("rider"));

        MaterialButton btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(v -> {
            if ("pharmacy".equals(selectedTarget)) {
                startActivity(new Intent(this, ComplaintPharmacyActivity.class));
            } else {
                startActivity(new Intent(this, ComplaintRiderActivity.class));
            }
        });
    }

    private void selectTarget(String target) {
        selectedTarget = target;
        int selectedBg = getResources().getColor(R.color.pg_primary_light, null);
        int normalBg   = getResources().getColor(R.color.pg_card, null);
        optionPharmacy.setCardBackgroundColor("pharmacy".equals(target) ? selectedBg : normalBg);
        optionRider.setCardBackgroundColor("rider".equals(target) ? selectedBg : normalBg);
    }
}
