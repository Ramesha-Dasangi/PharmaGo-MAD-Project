package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignRiderActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    
    private String orderId; 
    private String oldRiderId;
    private UserModel selectedRider = null;

    private TextView tvOrderNumber, tvOrderDetails, tvEmptyRiders;
    private ProgressBar progressRiders;
    private RecyclerView rvRiders;
    private MaterialButton btnAssignRider;
    private RiderAssignmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_rider);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        tvOrderNumber = findViewById(R.id.tvOrderNumber);
        tvOrderDetails = findViewById(R.id.tvOrderDetails);
        tvEmptyRiders = findViewById(R.id.tvEmptyRiders);
        progressRiders = findViewById(R.id.progressRiders);
        rvRiders = findViewById(R.id.rvRiders);
        btnAssignRider = findViewById(R.id.btnAssignRider);

        Intent intent = getIntent();
        if (intent != null) {
            orderId = intent.getStringExtra("ORDER_ID");
            oldRiderId = intent.getStringExtra("OLD_RIDER_ID");
            
            String displayId = intent.getStringExtra("ORDER_DISPLAY_ID");
            if (displayId == null) displayId = orderId;
            if (displayId != null) tvOrderNumber.setText(displayId);
            
            String details = intent.getStringExtra("ORDER_DETAILS");
            if (details != null) tvOrderDetails.setText(details);
        }

        if (orderId == null) {
            Toast.makeText(this, "Error: Missing Order ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupRecyclerView();
        loadApprovedRiders();
        setupBottomNav();

        btnAssignRider.setOnClickListener(v -> assignRiderToOrder());
    }

    private void setupRecyclerView() {
        adapter = new RiderAssignmentAdapter();
        rvRiders.setLayoutManager(new LinearLayoutManager(this));
        rvRiders.setAdapter(adapter);

        adapter.setListener(rider -> {
            selectedRider = rider;
            btnAssignRider.setEnabled(true);
            btnAssignRider.setText("Assign " + rider.getName());
        });
    }

    private void loadApprovedRiders() {
        progressRiders.setVisibility(View.VISIBLE);
        rvRiders.setVisibility(View.GONE);
        tvEmptyRiders.setVisibility(View.GONE);

        db.collection("riders")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressRiders.setVisibility(View.GONE);
                    List<UserModel> riders = new ArrayList<>();
                    
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Skip if the rider already has an active order
                        String activeOrderId = doc.getString("activeOrderId");
                        if (activeOrderId != null && !activeOrderId.isEmpty()) {
                            continue; 
                        }

                        UserModel u = new UserModel();
                        u.setId(doc.getId());
                        u.setName(doc.getString("name"));
                        u.setPhone(doc.getString("phone"));
                        u.setRole("rider");
                        u.setStatus("approved");
                        riders.add(u);
                    }
                    
                    adapter.setRiders(riders);
                    
                    if (riders.isEmpty()) {
                        tvEmptyRiders.setVisibility(View.VISIBLE);
                        rvRiders.setVisibility(View.GONE);
                    } else {
                        rvRiders.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressRiders.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load riders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void assignRiderToOrder() {
        if (selectedRider == null) return;
        
        btnAssignRider.setEnabled(false);
        btnAssignRider.setText("Assigning...");

        com.google.firebase.firestore.WriteBatch batch = db.batch();

        Map<String, Object> orderUpdates = new HashMap<>();
        orderUpdates.put("riderId", selectedRider.getId());
        orderUpdates.put("riderName", selectedRider.getName());
        orderUpdates.put("status", "assigned"); 
        
        batch.set(db.collection("orders").document(orderId), orderUpdates, SetOptions.merge());

        Map<String, Object> riderUpdates = new HashMap<>();
        riderUpdates.put("activeOrderId", orderId);

        batch.set(db.collection("riders").document(selectedRider.getId()), riderUpdates, SetOptions.merge());
        batch.set(db.collection("users").document(selectedRider.getId()), riderUpdates, SetOptions.merge());

        // Remove activeOrderId from old rider if this is a reassignment
        if (oldRiderId != null && !oldRiderId.isEmpty()) {
            Map<String, Object> oldRiderUpdates = new HashMap<>();
            oldRiderUpdates.put("activeOrderId", com.google.firebase.firestore.FieldValue.delete());
            batch.update(db.collection("riders").document(oldRiderId), oldRiderUpdates);
            batch.update(db.collection("users").document(oldRiderId), oldRiderUpdates);
        }

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Rider assigned successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnAssignRider.setEnabled(true);
                    btnAssignRider.setText("Assign " + selectedRider.getName());
                    Toast.makeText(this, "Failed to assign rider: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_delivery);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_approvals) {
                startActivity(new Intent(this, PendingApprovalsActivity.class));
                return true;
            } else if (itemId == R.id.nav_delivery) {
                startActivity(new Intent(this, UnassignedOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_complaints) {
                startActivity(new Intent(this, ComplaintsActivity.class));
                return true;
            }
            return false;
        });
    }
}
