package com.nibm.pharmagomadproject.customer;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nibm.pharmagomadproject.R;

public class ReportIssueActivity extends AppCompatActivity {

    private CardView optionPharmacy, optionRider;
    private String selectedTarget = "pharmacy"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_issue);
        getSupportActionBar().hide();

        MaterialCardView optionPharmacy = findViewById(R.id.optionPharmacy);
        MaterialCardView optionRider    = findViewById(R.id.optionRider);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Select pharmacy
        optionPharmacy.setOnClickListener(v -> {
            selectedTarget = "pharmacy";
            optionPharmacy.setCardBackgroundColor(
                    getResources().getColor(R.color.pg_primary_light, null));
            optionPharmacy.setStrokeColor(
                    getResources().getColorStateList(R.color.pg_primary, null));
            optionPharmacy.setStrokeWidth(4);

            optionRider.setCardBackgroundColor(
                    getResources().getColor(R.color.pg_card, null));
            optionRider.setStrokeColor(
                    getResources().getColorStateList(R.color.pg_border, null));
            optionRider.setStrokeWidth(2);
        });

        // Select rider
        optionRider.setOnClickListener(v -> {
            selectedTarget = "rider";
            optionRider.setCardBackgroundColor(
                    getResources().getColor(R.color.pg_primary_light, null));
            optionRider.setStrokeColor(
                    getResources().getColorStateList(R.color.pg_primary, null));
            optionRider.setStrokeWidth(4);

            optionPharmacy.setCardBackgroundColor(
                    getResources().getColor(R.color.pg_card, null));
            optionPharmacy.setStrokeColor(
                    getResources().getColorStateList(R.color.pg_border, null));
            optionPharmacy.setStrokeWidth(2);
        });

        // Continue button
        MaterialButton btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(v -> {
            Intent intent;
            if (selectedTarget.equals("pharmacy")) {
                intent = new Intent(this, ComplaintPharmacyActivity.class);
            } else {
                intent = new Intent(this, ComplaintRiderActivity.class);
            }
            intent.putExtra("target", selectedTarget);
            startActivity(intent);
        });
    }
}
