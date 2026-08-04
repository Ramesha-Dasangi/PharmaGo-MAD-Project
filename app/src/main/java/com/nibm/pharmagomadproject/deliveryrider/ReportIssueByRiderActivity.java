package com.nibm.pharmagomadproject.deliveryrider;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

import java.util.HashMap;
import java.util.Map;

public class ReportIssueByRiderActivity extends AppCompatActivity {

    private String selectedChip = "Pharmacy closed";
    private String orderId = "";
    private String targetName = "Pharmacy"; // defaults to pharmacy since rider delivers from there
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_issue_by_rider);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) orderId = "";
        
        if (!orderId.isEmpty()) {
            db.collection("orders").document(orderId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String pName = doc.getString("pharmacyName");
                        if (pName != null && !pName.isEmpty()) {
                            targetName = pName;
                        }
                    }
                });
        }

        int[] chipIds   = { R.id.chipPharmacyClosed, R.id.chipCustomerUnavailable, R.id.chipOrderNotReady,
                             R.id.chipWrongAddressRider, R.id.chipLongWaitTime, R.id.chipOtherIssue };
        String[] labels = { "Pharmacy closed", "Customer unavailable", "Order not ready",
                             "Wrong address", "Long wait time", "Other" };
        setupChips(chipIds, labels);

        MaterialButton btnSubmit = findViewById(R.id.btnSubmitReport);
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

        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        String complaintId = "CMP-" + System.currentTimeMillis();

        Map<String, Object> complaint = new HashMap<>();
        complaint.put("complaintId", complaintId);
        complaint.put("customerId", uid); // Actually Rider's UID here
        complaint.put("orderId", orderId);
        complaint.put("type", "pharmacy"); // Rider complaining about pharmacy/customer
        complaint.put("targetName", targetName);
        complaint.put("category", selectedChip);
        complaint.put("description", desc);
        complaint.put("status", "pending");
        complaint.put("createdAt", System.currentTimeMillis());

        MaterialButton btnSubmit = findViewById(R.id.btnSubmitReport);
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        db.collection("complaints")
                .document(complaintId)
                .set(complaint)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Complaint submitted successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Submit Complaint");
                    Toast.makeText(this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
