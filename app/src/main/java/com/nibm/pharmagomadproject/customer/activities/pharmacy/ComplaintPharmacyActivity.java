package com.nibm.pharmagomadproject.customer.activities.pharmacy;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.LinearLayout;
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

public class ComplaintPharmacyActivity extends AppCompatActivity {

    private String selectedPharmacy = "Pharmacy";
    private String selectedChip     = "Wrong medicine";
    private String orderId = "";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complaint_pharmacy);
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
                            // Collect all unique pharmacy names from items
                            java.util.List<String> pharmNames = new java.util.ArrayList<>();
                            java.util.List<?> items = (java.util.List<?>) doc.get("items");
                            if (items != null) {
                                for (Object obj : items) {
                                    if (obj instanceof java.util.Map) {
                                        Object pn = ((java.util.Map<?, ?>) obj).get("pharmacyName");
                                        if (pn != null && !pn.toString().isEmpty()
                                                && !pharmNames.contains(pn.toString())) {
                                            pharmNames.add(pn.toString());
                                        }
                                    }
                                }
                            }
                            // Fallback to top-level pharmacyName (may be comma-separated)
                            if (pharmNames.isEmpty()) {
                                String pName = doc.getString("pharmacyName");
                                if (pName != null && !pName.isEmpty()) {
                                    if (pName.contains(",")) {
                                        for (String s : pName.split(","))
                                            if (!s.trim().isEmpty()) pharmNames.add(s.trim());
                                    } else {
                                        pharmNames.add(pName);
                                    }
                                }
                            }

                            TextView tvPharmacyName = findViewById(R.id.tvPharmacyName);
                            if (pharmNames.size() == 1) {
                                selectedPharmacy = pharmNames.get(0);
                                if (tvPharmacyName != null) tvPharmacyName.setText(selectedPharmacy);
                            } else if (pharmNames.size() > 1) {
                                // Multi-pharmacy — let user pick
                                selectedPharmacy = pharmNames.get(0);
                                if (tvPharmacyName != null) {
                                    tvPharmacyName.setText(selectedPharmacy + "  ▼");
                                    final java.util.List<String> finalNames = pharmNames;
                                    tvPharmacyName.setOnClickListener(v -> {
                                        String[] arr = finalNames.toArray(new String[0]);
                                        new android.app.AlertDialog.Builder(ComplaintPharmacyActivity.this)
                                            .setTitle("Select pharmacy")
                                            .setItems(arr, (dialog, which) -> {
                                                selectedPharmacy = arr[which];
                                                tvPharmacyName.setText(selectedPharmacy + "  ▼");
                                            })
                                            .show();
                                    });
                                }
                            }
                        }
                    });
        }

        // Issue chips
        int[] chipIds   = { R.id.chipWrongMedicine, R.id.chipFake, R.id.chipIncorrectPrice,
                             R.id.chipExpired, R.id.chipRxNotVerified, R.id.chipOther };
        String[] labels = { "Wrong medicine", "Fake / unavailable listing",
                             "Incorrect price", "Expired medicine",
                             "Prescription not verified", "Other" };
        setupChips(chipIds, labels);

        // Submit
        MaterialButton btnSubmit = findViewById(R.id.btnSubmitComplaint);
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
        // Default select first
        chips[0].performClick();
    }

    private void submitComplaint() {
        TextInputEditText etDesc = findViewById(R.id.etDescribeIssue);
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
        complaint.put("customerId", uid);
        complaint.put("orderId", orderId);
        complaint.put("type", "pharmacy");
        complaint.put("targetName", selectedPharmacy);
        complaint.put("category", selectedChip);
        complaint.put("description", desc);
        complaint.put("status", "pending");
        complaint.put("createdAt", System.currentTimeMillis());

        MaterialButton btnSubmit = findViewById(R.id.btnSubmitComplaint);
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
