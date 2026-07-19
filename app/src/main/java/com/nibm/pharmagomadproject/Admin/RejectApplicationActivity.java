package com.nibm.pharmagomadproject.Admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.nibm.pharmagomadproject.R;

import java.util.HashMap;
import java.util.Map;

public class RejectApplicationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String pharmacyId;
    private String ownerId;

    private ChipGroup chipGroupReasons;
    private EditText etReasonNotes;
    private MaterialButton btnConfirmReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reject_application);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        pharmacyId = getIntent().getStringExtra("pharmacyId");
        ownerId = getIntent().getStringExtra("ownerId");
        String pharmacyName = getIntent().getStringExtra("pharmacyName");

        TextView tvWarningMessage = findViewById(R.id.tvWarningMessage);
        if (pharmacyName != null && !pharmacyName.isEmpty()) {
            tvWarningMessage.setText(pharmacyName + " will be notified with this reason and can re-apply.");
        }

        chipGroupReasons = findViewById(R.id.chipGroupReasons);
        etReasonNotes = findViewById(R.id.etReasonNotes);
        btnConfirmReject = findViewById(R.id.btnConfirmReject);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        btnConfirmReject.setOnClickListener(v -> confirmReject());
    }

    private void confirmReject() {
        if (pharmacyId == null || ownerId == null) {
            Toast.makeText(this, "Missing application reference", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        StringBuilder reason = new StringBuilder();
        int checkedId = chipGroupReasons.getCheckedChipId();
        if (checkedId != -1) {
            Chip checkedChip = findViewById(checkedId);
            if (checkedChip != null) {
                reason.append(checkedChip.getText());
            }
        }

        String notes = etReasonNotes.getText().toString().trim();
        if (!notes.isEmpty()) {
            if (reason.length() > 0) reason.append(" - ");
            reason.append(notes);
        }

        if (reason.length() == 0) {
            Toast.makeText(this, "Please select a reason or add a note", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmReject.setEnabled(false);

        WriteBatch batch = db.batch();

        Map<String, Object> pharmacyUpdate = new HashMap<>();
        pharmacyUpdate.put("isApproved", false);
        pharmacyUpdate.put("status", "rejected");
        pharmacyUpdate.put("rejectionReason", reason.toString());
        pharmacyUpdate.put("rejectedAt", Timestamp.now());
        batch.update(db.collection("pharmacies").document(pharmacyId), pharmacyUpdate);

        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("isApproved", false);
        userUpdate.put("status", "rejected");
        batch.update(db.collection("users").document(ownerId), userUpdate);

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Application rejected successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnConfirmReject.setEnabled(true);
                    Toast.makeText(this, "Rejection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
