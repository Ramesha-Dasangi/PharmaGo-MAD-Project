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

public class ComplaintRiderActivity extends AppCompatActivity {

    private String selectedChip = "Late delivery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complaint_rider);
        getSupportActionBar().hide();

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Issue chips
        TextView[] chips = {
                findViewById(R.id.chipLateDelivery),
                findViewById(R.id.chipRudeBehavior),
                findViewById(R.id.chipItemMissing),
                findViewById(R.id.chipWrongAddress),
                findViewById(R.id.chipStatusNotUpdated),
                findViewById(R.id.chipOtherRider)
        };
        String[] labels = {
                "Late delivery", "Rude behavior",
                "Item missing / damaged", "Wrong address",
                "Status not updated", "Other"
        };
        for (int i = 0; i < chips.length; i++) {
            final String label = labels[i];
            chips[i].setOnClickListener(v -> selectChip(label, chips, labels));
        }

        // Submit
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitComplaintRider);
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
        TextInputEditText etDesc = findViewById(R.id.etDescribeIssueRider);
        String desc = etDesc.getText() != null
                ? etDesc.getText().toString().trim() : "";

        if (TextUtils.isEmpty(desc)) {
            etDesc.setError("Please describe the issue");
            etDesc.requestFocus();
            return;
        }

        // TODO: save complaint to Firebase
        // complaint: { targetType: "rider", targetId: "riderId",
        //              reason: selectedChip, description: desc }
        Toast.makeText(this,
                "Complaint submitted. We'll review it shortly.",
                Toast.LENGTH_LONG).show();
        finish();
    }
}
