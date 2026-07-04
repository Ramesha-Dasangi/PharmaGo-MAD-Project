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
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        int[] chipIds   = { R.id.chipLateDelivery, R.id.chipRudeBehavior, R.id.chipItemMissing,
                             R.id.chipWrongAddress, R.id.chipStatusNotUpdated, R.id.chipOtherRider };
        String[] labels = { "Late delivery", "Rude behavior", "Item missing / damaged",
                             "Wrong address", "Status not updated", "Other" };
        setupChips(chipIds, labels);

        MaterialButton btnSubmit = findViewById(R.id.btnSubmitComplaintRider);
        btnSubmit.setOnClickListener(v -> submitComplaint());
    }

    private void setupChips(int[] ids, String[] labels) {
        TextView[] chips = new TextView[ids.length];
        for (int i = 0; i < ids.length; i++) chips[i] = findViewById(ids[i]);
        for (int i = 0; i < ids.length; i++) {
            final String label = labels[i];
            chips[i].setOnClickListener(v -> {
                selectedChip = label;
                int primary = getResources().getColor(R.color.pg_primary, null);
                int sub     = getResources().getColor(R.color.pg_sub, null);
                for (int j = 0; j < chips.length; j++) {
                    boolean sel = labels[j].equals(label);
                    chips[j].setBackgroundResource(sel ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
                    chips[j].setTextColor(sel ? primary : sub);
                }
            });
        }
        chips[0].performClick();
    }

    private void submitComplaint() {
        TextInputEditText etDesc = findViewById(R.id.etDescribeIssueRider);
        String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
        if (TextUtils.isEmpty(desc)) {
            etDesc.setError("Please describe the issue");
            etDesc.requestFocus();
            return;
        }
        Toast.makeText(this, "Complaint submitted successfully!", Toast.LENGTH_LONG).show();
        finish();
    }
}
