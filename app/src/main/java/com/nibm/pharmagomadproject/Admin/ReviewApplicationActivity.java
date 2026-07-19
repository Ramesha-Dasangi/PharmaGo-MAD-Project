package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.nibm.pharmagomadproject.R;

import java.util.HashMap;
import java.util.Map;

public class ReviewApplicationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String pharmacyId;
    private String ownerId;
    private String pharmacyName;
    private String licenseImageUrl;

    private TextView tvPharmacyName, tvOwnerName, tvLicenseNo, tvPhone, tvAddress, tvLicenseFileName;
    private MaterialButton btnApprove, btnReject;
    private View progressBar, scrollContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_application);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        pharmacyId = getIntent().getStringExtra("pharmacyId");

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        tvPharmacyName = findViewById(R.id.tvPharmacyName);
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvLicenseNo = findViewById(R.id.tvLicenseNo);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvLicenseFileName = findViewById(R.id.tvLicenseFileName);
        progressBar = findViewById(R.id.progressBar);
        scrollContent = findViewById(R.id.scrollContent);

        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);

        MaterialCardView cardLicense = findViewById(R.id.cardLicense);
        cardLicense.setOnClickListener(v -> {
            if (licenseImageUrl != null && !licenseImageUrl.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(licenseImageUrl)));
            } else {
                Toast.makeText(this, "License document not available", Toast.LENGTH_SHORT).show();
            }
        });

        btnApprove.setOnClickListener(v -> approveApplication());

        btnReject.setOnClickListener(v -> {
            if (ownerId == null || pharmacyId == null) {
                Toast.makeText(this, "Application data not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ReviewApplicationActivity.this, RejectApplicationActivity.class);
            intent.putExtra("pharmacyId", pharmacyId);
            intent.putExtra("ownerId", ownerId);
            intent.putExtra("pharmacyName", pharmacyName);
            startActivity(intent);
            finish();
        });

        loadApplication();
    }

    private void loadApplication() {
        if (pharmacyId == null) {
            Toast.makeText(this, "Missing pharmacy reference", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);

        db.collection("pharmacies").document(pharmacyId).get()
                .addOnSuccessListener(this::bindPharmacy)
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Failed to load application: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void bindPharmacy(DocumentSnapshot doc) {
        setLoading(false);

        if (!doc.exists()) {
            Toast.makeText(this, "This application no longer exists", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pharmacyName = doc.getString("name");
        ownerId = doc.getString("ownerId");
        licenseImageUrl = doc.getString("licenseImageUrl");

        tvPharmacyName.setText(pharmacyName != null ? pharmacyName : "-");
        tvOwnerName.setText(doc.getString("ownerName") != null ? doc.getString("ownerName") : "-");
        tvLicenseNo.setText(doc.getString("licenseNo") != null ? doc.getString("licenseNo") : "-");
        tvPhone.setText(doc.getString("phone") != null ? doc.getString("phone") : "-");
        tvAddress.setText(doc.getString("address") != null ? doc.getString("address") : "-");
        tvLicenseFileName.setText(licenseImageUrl != null && !licenseImageUrl.isEmpty()
                ? "Tap to view uploaded license"
                : "No license document uploaded");

        String status = doc.getString("status");
        boolean alreadyDecided = status != null && !status.equals("pending");
        btnApprove.setEnabled(!alreadyDecided);
        btnReject.setEnabled(!alreadyDecided);
        if (alreadyDecided) {
            Toast.makeText(this, "This application was already " + status, Toast.LENGTH_SHORT).show();
        }
    }

    private void approveApplication() {
        if (pharmacyId == null || ownerId == null) {
            Toast.makeText(this, "Application data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        btnApprove.setEnabled(false);
        btnReject.setEnabled(false);

        WriteBatch batch = db.batch();

        Map<String, Object> pharmacyUpdate = new HashMap<>();
        pharmacyUpdate.put("isApproved", true);
        pharmacyUpdate.put("status", "approved");
        batch.set(db.collection("pharmacies").document(pharmacyId), pharmacyUpdate, SetOptions.merge());

        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("isApproved", true);
        userUpdate.put("status", "approved");
        batch.set(db.collection("users").document(ownerId), userUpdate, SetOptions.merge());

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Pharmacy approved. Owner can now log in.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnApprove.setEnabled(true);
                    btnReject.setEnabled(true);
                    Toast.makeText(this, "Approval failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        scrollContent.setVisibility(loading ? View.GONE : View.VISIBLE);
    }
}
