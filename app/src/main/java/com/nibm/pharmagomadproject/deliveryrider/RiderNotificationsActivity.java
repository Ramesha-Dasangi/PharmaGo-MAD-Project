package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

import java.util.List;
import java.util.Map;

public class RiderNotificationsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private LinearLayout containerNotifications;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_notifications);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        containerNotifications = findViewById(R.id.containerNotifications);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadNotifications();
    }

    private void loadNotifications() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("orders")
                .whereEqualTo("riderId", uid)
                .whereEqualTo("status", "assigned")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        return;
                    }
                    tvEmptyState.setVisibility(View.GONE);
                    containerNotifications.removeAllViews();
                    containerNotifications.addView(tvEmptyState); // Keep it around just in case

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        View card = getLayoutInflater().inflate(R.layout.item_notification_card, containerNotifications, false);
                        
                        TextView tvTitle = card.findViewById(R.id.tvNotifTitle);
                        TextView tvDesc = card.findViewById(R.id.tvNotifDesc);
                        
                        String orderId = doc.getId();
                        String shortId = "#" + orderId.substring(0, Math.min(8, orderId.length())).toUpperCase();
                        
                        tvTitle.setText("New Assignment: " + shortId);
                        
                        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                        int stops = items != null ? items.size() : 1;
                        tvDesc.setText("You have a new delivery assignment with " + stops + " items. Tap to view details.");
                        
                        card.setOnClickListener(v -> {
                            Intent intent = new Intent(RiderNotificationsActivity.this, AssignmentDetailsActivity.class);
                            intent.putExtra("orderId", orderId);
                            startActivity(intent);
                        });
                        
                        containerNotifications.addView(card);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    tvEmptyState.setVisibility(View.VISIBLE);
                });
    }
}
