package com.nibm.pharmagomadproject.Admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.nibm.pharmagomadproject.R;

public class RejectRiderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reject_rider);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String riderId = getIntent().getStringExtra("riderId");

        MaterialButton btnConfirmReject = findViewById(R.id.btnConfirmReject);
        btnConfirmReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (riderId == null) {
                    Toast.makeText(RejectRiderActivity.this, "Rider ID not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                com.google.android.material.chip.ChipGroup chipGroup = findViewById(R.id.chipGroupReasonsRider);
                StringBuilder fullReason = new StringBuilder();
                int checkedId = chipGroup.getCheckedChipId();
                if (checkedId != -1) {
                    com.google.android.material.chip.Chip checkedChip = findViewById(checkedId);
                    if (checkedChip != null) {
                        fullReason.append(checkedChip.getText());
                    }
                }
                
                android.widget.EditText etRejectionReason = findViewById(R.id.etRejectionReason);
                String notes = etRejectionReason.getText().toString().trim();
                if (!notes.isEmpty()) {
                    if (fullReason.length() > 0) fullReason.append(" - ");
                    fullReason.append(notes);
                }

                if (fullReason.length() == 0) {
                    Toast.makeText(RejectRiderActivity.this, "Please select a reason or add a note", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String reason = fullReason.toString();
                
                btnConfirmReject.setEnabled(false);
                
                com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                com.google.firebase.firestore.WriteBatch batch = db.batch();

                java.util.Map<String, Object> updateData = new java.util.HashMap<>();
                updateData.put("isApproved", false);
                updateData.put("status", "rejected");
                updateData.put("rejectionReason", reason);

                batch.set(db.collection("riders").document(riderId), updateData, com.google.firebase.firestore.SetOptions.merge());
                batch.set(db.collection("users").document(riderId), updateData, com.google.firebase.firestore.SetOptions.merge());

                batch.commit()
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(RejectRiderActivity.this, "Rider application rejected", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            btnConfirmReject.setEnabled(true);
                            Toast.makeText(RejectRiderActivity.this, "Failed to reject: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}
