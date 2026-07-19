package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.nibm.pharmagomadproject.R;

import java.util.HashMap;
import java.util.Map;

public class ReviewRiderActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String riderId;
    private String licenseUrl;

    private ProgressBar progressBar;
    private NestedScrollView scrollContent;
    private TextView tvRiderName, tvRiderSub, tvNicValue, tvEmailValue,
            tvPhoneValue, tvVehicleValue, tvVehicleRegValue, tvLicenseFileName;
    private MaterialCardView cardLicense;
    private MaterialButton btnAccept, btnReject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_rider);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();
        riderId = getIntent().getStringExtra("riderId");

        // Views
        progressBar = findViewById(R.id.progressBar);
        scrollContent = findViewById(R.id.scrollContent);
        tvRiderName = findViewById(R.id.tvRiderName);
        tvRiderSub = findViewById(R.id.tvRiderSub);
        tvNicValue = findViewById(R.id.tvNicValue);
        tvEmailValue = findViewById(R.id.tvEmailValue);
        tvPhoneValue = findViewById(R.id.tvPhoneValue);
        tvVehicleValue = findViewById(R.id.tvVehicleValue);
        tvVehicleRegValue = findViewById(R.id.tvVehicleRegValue);
        tvLicenseFileName = findViewById(R.id.tvLicenseFileName);
        cardLicense = findViewById(R.id.cardLicense);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        if (riderId == null) {
            Toast.makeText(this, "Rider data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadRiderData();
        setupButtons();
    }

    private void loadRiderData() {
        setLoading(true);

        db.collection("riders").document(riderId).get()
                .addOnSuccessListener(doc -> {
                    setLoading(false);
                    if (!doc.exists()) {
                        Toast.makeText(this, "Rider not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    populateUI(doc);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Failed to load rider: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateUI(DocumentSnapshot doc) {
        String name = doc.getString("name");
        String email = doc.getString("email");
        String phone = doc.getString("phone");
        String nic = doc.getString("nic");
        String vehicleType = doc.getString("vehicleType");
        String vehicleReg = doc.getString("vehicleReg");
        licenseUrl = doc.getString("licenseUrl");
        String status = doc.getString("status");

        tvRiderName.setText(name != null ? name : "Unknown Rider");

        // Sub line: phone + applied time
        String sub = (phone != null ? phone : "No phone");
        tvRiderSub.setText(sub);

        tvNicValue.setText(nic != null ? nic : "—");
        tvEmailValue.setText(email != null ? email : "—");
        tvPhoneValue.setText(phone != null ? phone : "—");

        // Vehicle: type — reg
        String vehicleStr = "";
        if (vehicleType != null) vehicleStr += vehicleType;
        if (vehicleReg != null) vehicleStr += (vehicleStr.isEmpty() ? "" : " — ") + vehicleReg;
        tvVehicleValue.setText(vehicleStr.isEmpty() ? "—" : vehicleType);
        tvVehicleRegValue.setText(vehicleReg != null ? vehicleReg : "—");

        // License doc
        if (licenseUrl != null && !licenseUrl.isEmpty()) {
            tvLicenseFileName.setText("Tap to view uploaded license");
            cardLicense.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(licenseUrl));
                startActivity(intent);
            });
        } else {
            tvLicenseFileName.setText("No license document uploaded");
            cardLicense.setOnClickListener(null);
        }

        // Disable buttons if already decided
        boolean alreadyDecided = "approved".equals(status) || "rejected".equals(status);
        btnAccept.setEnabled(!alreadyDecided);
        btnReject.setEnabled(!alreadyDecided);
        if (alreadyDecided) {
            Toast.makeText(this, "This application was already " + status, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtons() {
        btnReject.setOnClickListener(v -> {
            Intent intent = new Intent(ReviewRiderActivity.this, RejectRiderActivity.class);
            intent.putExtra("riderId", riderId);
            startActivity(intent);
            finish();
        });

        btnAccept.setOnClickListener(v -> {
            btnAccept.setEnabled(false);
            btnReject.setEnabled(false);

            com.google.firebase.firestore.WriteBatch batch = db.batch();

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("isApproved", true);
            updateData.put("status", "approved");

            batch.set(db.collection("riders").document(riderId), updateData, SetOptions.merge());
            batch.set(db.collection("users").document(riderId), updateData, SetOptions.merge());

            batch.commit()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Rider approved successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnAccept.setEnabled(true);
                        btnReject.setEnabled(true);
                        Toast.makeText(this, "Approval failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        scrollContent.setVisibility(loading ? View.GONE : View.VISIBLE);
    }
}
