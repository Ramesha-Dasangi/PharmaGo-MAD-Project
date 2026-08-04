package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ComplaintsActivity extends AppCompatActivity {

    private TextView tabOpen, tabResolved;
    private LinearLayout llOpen, llResolved;
    
    private RecyclerView rvOpenComplaints, rvResolvedComplaints;
    private ComplaintAdapter openAdapter, resolvedAdapter;
    private List<ComplaintModel> openList, resolvedList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        tabOpen = findViewById(R.id.tabOpen);
        tabResolved = findViewById(R.id.tabResolved);
        llOpen = findViewById(R.id.llOpen);
        llResolved = findViewById(R.id.llResolved);
        rvOpenComplaints = findViewById(R.id.rvOpenComplaints);
        rvResolvedComplaints = findViewById(R.id.rvResolvedComplaints);

        tabOpen.setOnClickListener(v -> selectTab(true));
        tabResolved.setOnClickListener(v -> selectTab(false));

        setupRecyclerViews();
        fetchComplaints();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_complaints);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ComplaintsActivity.this, AdminDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_approvals) {
                startActivity(new Intent(ComplaintsActivity.this, PendingApprovalsActivity.class));
                return true;
            } else if (itemId == R.id.nav_delivery) {
                startActivity(new Intent(ComplaintsActivity.this, UnassignedOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_complaints) {
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerViews() {
        openList = new ArrayList<>();
        resolvedList = new ArrayList<>();

        rvOpenComplaints.setLayoutManager(new LinearLayoutManager(this));
        rvResolvedComplaints.setLayoutManager(new LinearLayoutManager(this));

        ComplaintAdapter.OnComplaintActionListener actionListener = new ComplaintAdapter.OnComplaintActionListener() {
            @Override
            public void onResolve(ComplaintModel complaint) {
                resolveComplaint(complaint);
            }

            @Override
            public void onView(ComplaintModel complaint) {
                showComplaintDetails(complaint);
            }
        };

        openAdapter = new ComplaintAdapter(this, openList, actionListener);
        resolvedAdapter = new ComplaintAdapter(this, resolvedList, actionListener);

        rvOpenComplaints.setAdapter(openAdapter);
        rvResolvedComplaints.setAdapter(resolvedAdapter);
    }

    private void fetchComplaints() {
        db.collection("complaints")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load complaints.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    openList.clear();
                    resolvedList.clear();

                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            ComplaintModel complaint = doc.toObject(ComplaintModel.class);
                            if (complaint.getComplaintId() == null) {
                                complaint.setComplaintId(doc.getId());
                            }

                            if ("pending".equalsIgnoreCase(complaint.getStatus())) {
                                openList.add(complaint);
                            } else {
                                resolvedList.add(complaint);
                            }
                        }
                    }
                    
                    tabOpen.setText("Open (" + openList.size() + ")");
                    tabResolved.setText("Resolved (" + resolvedList.size() + ")");

                    openAdapter.notifyDataSetChanged();
                    resolvedAdapter.notifyDataSetChanged();
                });
    }

    private void resolveComplaint(ComplaintModel complaint) {
        EditText input = new EditText(this);
        input.setHint("Enter resolution message for user");
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        
        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(50, 20, 50, 0);
        layout.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Resolve Complaint")
                .setMessage("Are you sure you want to mark this complaint as resolved? An alert will be sent to the user.")
                .setView(layout)
                .setPositiveButton("Resolve", (dialog, which) -> {
                    String resolutionMessage = input.getText().toString().trim();
                    if (resolutionMessage.isEmpty()) {
                        resolutionMessage = "Your complaint has been resolved by our admin team.";
                    }
                    
                    String finalMessage = resolutionMessage;
                    db.collection("complaints").document(complaint.getComplaintId())
                            .update("status", "resolved", "resolutionMessage", finalMessage)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Complaint resolved", Toast.LENGTH_SHORT).show();
                                // Send notification to customer
                                if (complaint.getCustomerId() != null && !complaint.getCustomerId().isEmpty()) {
                                    Map<String, Object> notif = new HashMap<>();
                                    notif.put("userId", complaint.getCustomerId());
                                    notif.put("title", "Complaint Resolved");
                                    notif.put("message", finalMessage);
                                    notif.put("type", "complaint");
                                    notif.put("referenceId", complaint.getComplaintId());
                                    notif.put("isRead", false);
                                    notif.put("createdAt", FieldValue.serverTimestamp());
                                    notif.put("notificationId", UUID.randomUUID().toString());
                                    
                                    db.collection("notifications").document(notif.get("notificationId").toString())
                                        .set(notif);
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to resolve", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showComplaintDetails(ComplaintModel complaint) {
        String msg = "Target: " + complaint.getTargetName() + "\nType: " + complaint.getType();
        if (complaint.getOrderId() != null && !complaint.getOrderId().isEmpty()) {
            msg += "\nOrder ID: " + complaint.getOrderId();
        }
        msg += "\n\nDescription:\n" + complaint.getDescription();
        
        new AlertDialog.Builder(this)
                .setTitle(complaint.getCategory())
                .setMessage(msg)
                .setPositiveButton("Close", null)
                .show();
    }

    private void selectTab(boolean isOpen) {
        if (isOpen) {
            tabOpen.setBackgroundResource(R.drawable.tab_active_bg);
            tabOpen.setTextColor(Color.WHITE);
            tabOpen.setTypeface(null, Typeface.BOLD);
            
            tabResolved.setBackground(null);
            tabResolved.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            tabResolved.setTypeface(null, Typeface.NORMAL);
            
            llOpen.setVisibility(View.VISIBLE);
            llResolved.setVisibility(View.GONE);
        } else {
            tabResolved.setBackgroundResource(R.drawable.tab_active_bg);
            tabResolved.setTextColor(Color.WHITE);
            tabResolved.setTypeface(null, Typeface.BOLD);
            
            tabOpen.setBackground(null);
            tabOpen.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
            tabOpen.setTypeface(null, Typeface.NORMAL);
            
            llResolved.setVisibility(View.VISIBLE);
            llOpen.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_complaints);
        }
    }
}
