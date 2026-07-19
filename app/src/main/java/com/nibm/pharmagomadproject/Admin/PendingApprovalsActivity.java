package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.List;

public class PendingApprovalsActivity extends AppCompatActivity {

    private static final String TAG = "PendingApprovals";

    private FirebaseFirestore db;
    private ListenerRegistration pharmacyListener;
    private ListenerRegistration riderListener;

    private PendingPharmacyAdapter pharmacyAdapter;
    private PendingRiderAdapter riderAdapter;
    private RecyclerView rvPharmacies, rvRiders;
    private ProgressBar progressPharmacies, progressRiders;
    private TextView tvEmptyPharmacies, tvEmptyRiders;
    private TextView tabPharmacies, tabRiders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_approvals);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        // Pharmacies list setup
        rvPharmacies = findViewById(R.id.rvPharmacies);
        progressPharmacies = findViewById(R.id.progressPharmacies);
        tvEmptyPharmacies = findViewById(R.id.tvEmptyPharmacies);

        rvPharmacies.setLayoutManager(new LinearLayoutManager(this));
        pharmacyAdapter = new PendingPharmacyAdapter(this::openReview);
        rvPharmacies.setAdapter(pharmacyAdapter);

        // Riders list setup
        rvRiders = findViewById(R.id.rvRiders);
        progressRiders = findViewById(R.id.progressRiders);
        tvEmptyRiders = findViewById(R.id.tvEmptyRiders);

        rvRiders.setLayoutManager(new LinearLayoutManager(this));
        riderAdapter = new PendingRiderAdapter(this::openReviewRider);
        rvRiders.setAdapter(riderAdapter);

        tabRiders = findViewById(R.id.tabRiders);
        tabPharmacies = findViewById(R.id.tabPharmacies);
        LinearLayout llPharmacies = findViewById(R.id.llPharmacies);
        LinearLayout llRiders = findViewById(R.id.llRiders);

        tabPharmacies.setOnClickListener(v -> {
            tabPharmacies.setBackgroundResource(R.drawable.tab_active_bg);
            tabPharmacies.setTextColor(Color.WHITE);
            tabPharmacies.setTypeface(null, android.graphics.Typeface.BOLD);

            tabRiders.setBackground(null);
            tabRiders.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            tabRiders.setTypeface(null, android.graphics.Typeface.NORMAL);

            llPharmacies.setVisibility(View.VISIBLE);
            llRiders.setVisibility(View.GONE);
        });

        tabRiders.setOnClickListener(v -> {
            tabRiders.setBackgroundResource(R.drawable.tab_active_bg);
            tabRiders.setTextColor(Color.WHITE);
            tabRiders.setTypeface(null, android.graphics.Typeface.BOLD);

            tabPharmacies.setBackground(null);
            tabPharmacies.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            tabPharmacies.setTypeface(null, android.graphics.Typeface.NORMAL);

            llPharmacies.setVisibility(View.GONE);
            llRiders.setVisibility(View.VISIBLE);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_approvals);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(PendingApprovalsActivity.this, AdminDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_approvals) {
                return true;
            } else if (itemId == R.id.nav_delivery) {
                startActivity(new Intent(PendingApprovalsActivity.this, UnassignedOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_complaints) {
                startActivity(new Intent(PendingApprovalsActivity.this, ComplaintsActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenPendingPharmacies();
        listenPendingRiders();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (pharmacyListener != null) {
            pharmacyListener.remove();
            pharmacyListener = null;
        }
        if (riderListener != null) {
            riderListener.remove();
            riderListener = null;
        }
    }

    private void openReview(PendingPharmacyModel pharmacy) {
        Intent intent = new Intent(PendingApprovalsActivity.this, ReviewApplicationActivity.class);
        intent.putExtra("pharmacyId", pharmacy.getId());
        startActivity(intent);
    }

    private void openReviewRider(PendingRiderModel rider) {
        Intent intent = new Intent(PendingApprovalsActivity.this, ReviewRiderActivity.class);
        intent.putExtra("riderId", rider.getId());
        startActivity(intent);
    }

    private void listenPendingPharmacies() {
        progressPharmacies.setVisibility(View.VISIBLE);
        tvEmptyPharmacies.setVisibility(View.GONE);

        // Fetch all, filter client-side to catch null/missing status docs too
        pharmacyListener = db.collection("pharmacies").addSnapshotListener((snapshots, error) -> {
            progressPharmacies.setVisibility(View.GONE);

            if (error != null) {
                Log.e(TAG, "Failed to load pending pharmacies", error);
                Toast.makeText(this, "Failed to load pharmacies: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            List<PendingPharmacyModel> pharmacies = new ArrayList<>();
            if (snapshots != null) {
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                    String status = doc.getString("status");
                    // Show if status is pending, null, or empty (not yet explicitly approved/rejected)
                    if ("approved".equals(status) || "rejected".equals(status)) continue;
                    PendingPharmacyModel model = doc.toObject(PendingPharmacyModel.class);
                    if (model != null) {
                        model.setId(doc.getId());
                        pharmacies.add(model);
                    }
                }
            }

            pharmacyAdapter.setItems(pharmacies);
            tabPharmacies.setText("Pharmacies (" + pharmacies.size() + ")");
            tvEmptyPharmacies.setVisibility(pharmacies.isEmpty() ? View.VISIBLE : View.GONE);
            rvPharmacies.setVisibility(pharmacies.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private void listenPendingRiders() {
        progressRiders.setVisibility(View.VISIBLE);
        tvEmptyRiders.setVisibility(View.GONE);

        // Fetch all, filter client-side to catch null/missing status docs too
        riderListener = db.collection("riders").addSnapshotListener((snapshots, error) -> {
            progressRiders.setVisibility(View.GONE);

            if (error != null) {
                Log.e(TAG, "Failed to load pending riders", error);
                Toast.makeText(this, "Failed to load riders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            List<PendingRiderModel> riders = new ArrayList<>();
            if (snapshots != null) {
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                    String status = doc.getString("status");
                    // Show if status is pending, null, or empty (not yet explicitly approved/rejected)
                    if ("approved".equals(status) || "rejected".equals(status)) continue;
                    PendingRiderModel model = doc.toObject(PendingRiderModel.class);
                    if (model != null) {
                        model.setId(doc.getId());
                        riders.add(model);
                    }
                }
            }

            riderAdapter.setItems(riders);
            tabRiders.setText("Riders (" + riders.size() + ")");
            tvEmptyRiders.setVisibility(riders.isEmpty() ? View.VISIBLE : View.GONE);
            rvRiders.setVisibility(riders.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }
}
