package com.nibm.pharmagomadproject.customer;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nibm.pharmagomadproject.R;

public class ComplaintPharmacyActivity extends AppCompatActivity {

    private String selectedPharmacy = "MediCare Pharmacy";
    private String selectedChip     = "Wrong medicine";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complaint_pharmacy);

        getSupportActionBar().hide();

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Pharmacy selection
        android.view.View optMediCare  = findViewById(R.id.optMediCare);
        android.view.View optCityPharma = findViewById(R.id.optCityPharma);

        optMediCare.setOnClickListener(v -> {
            selectedPharmacy = "MediCare Pharmacy";
            optMediCare.setBackgroundResource(R.drawable.bg_selected_option);
            optCityPharma.setBackgroundResource(R.drawable.bg_unselected_option);
        });
        optCityPharma.setOnClickListener(v -> {
            selectedPharmacy = "City Pharma";
            optCityPharma.setBackgroundResource(R.drawable.bg_selected_option);
            optMediCare.setBackgroundResource(R.drawable.bg_unselected_option);
        });

        // Issue chips
        TextView[] chips = {
                findViewById(R.id.chipWrongMedicine),
                findViewById(R.id.chipFake),
                findViewById(R.id.chipIncorrectPrice),
                findViewById(R.id.chipExpired),
                findViewById(R.id.chipRxNotVerified),
                findViewById(R.id.chipOther)
        };
        String[] labels = {
                "Wrong medicine", "Fake / unavailable listing",
                "Incorrect price", "Expired medicine",
                "Prescription not verified", "Other"
        };
        for (int i = 0; i < chips.length; i++) {
            final String label = labels[i];
            chips[i].setOnClickListener(v -> selectChip(label, chips, labels));
        }

        // Submit
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitComplaint);
        btnSubmit.setOnClickListener(v -> submitComplaint());
    }

    private void selectChip(String label, TextView[] chips, String[] labels) {
        selectedChip = label;
        int primary = getResources().getColor(R.color.pg_primary, null);
        int sub     = getResources().getColor(R.color.pg_sub, null);
        for (int i = 0; i < chips.length; i++) {
            boolean sel = labels[i].equals(label);
            chips[i].setBackgroundResource(
                    sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            chips[i].setTextColor(sel ? primary : sub);
        }
    }

    private void submitComplaint() {
        TextInputEditText etDesc = findViewById(R.id.etDescribeIssue);
        String desc = etDesc.getText() != null
                ? etDesc.getText().toString().trim() : "";

        if (TextUtils.isEmpty(desc)) {
            etDesc.setError("Please describe the issue");
            etDesc.requestFocus();
            return;
        }

        // TODO: save complaint to Firebase
        // complaint: { targetType: "pharmacy", targetId: selectedPharmacy,
        //              reason: selectedChip, description: desc }
        Toast.makeText(this,
                "Complaint submitted. We'll review it shortly.",
                Toast.LENGTH_LONG).show();
        finish();
    }
}