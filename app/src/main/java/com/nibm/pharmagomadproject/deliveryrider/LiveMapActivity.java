package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

import java.util.List;
import java.util.Map;

public class LiveMapActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_map);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupButtons();
        loadActiveOrderDetails();
    }

    private void loadActiveOrderDetails() {
        String currentUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUid == null) return;

        // Find the info card TextViews from the XML layout
        TextView tvCurrentOrderId = null;
        android.widget.LinearLayout infoCards = findViewById(R.id.infoCards);
        if (infoCards != null && infoCards.getChildCount() >= 2) {
            // First card: Current Order
            View card1 = infoCards.getChildAt(0);
            if (card1 instanceof android.widget.LinearLayout) {
                android.widget.LinearLayout ll1 = (android.widget.LinearLayout) card1;
                if (ll1.getChildCount() >= 2) {
                    tvCurrentOrderId = (TextView) ll1.getChildAt(1);
                }
            }
        }

        final TextView tvOrderId = tvCurrentOrderId;
        final TextView tvDropoffLabel = findViewById(R.id.tvDropoffLabel);
        final TextView tvDropoffValue = findViewById(R.id.tvDropoffValue);

        // Set defaults
        if (tvOrderId != null) tvOrderId.setText("No active order");
        if (tvDropoffValue != null) tvDropoffValue.setText("—");

        // Query for active orders assigned to this rider
        db.collection("orders")
                .whereEqualTo("riderId", currentUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String status = doc.getString("status");
                        if (status == null) continue;

                        // Show the first active order
                        if (status.equalsIgnoreCase("assigned") || status.equalsIgnoreCase("picked_up") || status.equalsIgnoreCase("out_for_delivery")) {
                            String docId = doc.getId();
                            String shortId = "#" + docId.substring(0, Math.min(8, docId.length())).toUpperCase();

                            if (tvOrderId != null) tvOrderId.setText(shortId);

                            String pharmName = doc.getString("pharmacyName");
                            String tempAddr = doc.getString("deliveryAddress");
                            if (tempAddr == null) tempAddr = doc.getString("address");
                            final String addr = tempAddr;

                            if (status.equalsIgnoreCase("assigned") || status.equalsIgnoreCase("picked_up")) {
                                if (tvDropoffLabel != null) tvDropoffLabel.setText("Pickup from");
                                if (tvDropoffValue != null) tvDropoffValue.setText(pharmName != null ? pharmName : "Pharmacy");
                            } else {
                                if (tvDropoffLabel != null) tvDropoffLabel.setText("Drop-off Customer");
                                if (tvDropoffValue != null) tvDropoffValue.setText(addr != null && !addr.isEmpty() ? addr : "No address");
                            }

                            // Show status in ETA/Distance area to replace the hacks
                            TextView tvEta = findViewById(R.id.tvEta);
                            if (tvEta != null) {
                                tvEta.setText(status.equalsIgnoreCase("out_for_delivery") ? "En route" : "Pending");
                            }
                            
                            // Show status in distance area
                            TextView tvDistance = findViewById(R.id.tvDistance);
                            if (tvDistance != null) {
                                String displayStatus = status.substring(0, 1).toUpperCase() + status.substring(1).replace("_", " ");
                                tvDistance.setText(displayStatus);
                            }

                            // Setup map navigation button based on status
                            Button btnRecenter = findViewById(R.id.btnRecenter);
                            if (btnRecenter != null) {
                                btnRecenter.setText("Open in Google Maps");
                                btnRecenter.setOnClickListener(v -> {
                                    if (status.equalsIgnoreCase("assigned") || status.equalsIgnoreCase("picked_up")) {
                                        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                                        if (items != null && !items.isEmpty()) {
                                            String pId = (String) items.get(0).get("pharmacyId");
                                            if (pId != null) {
                                                btnRecenter.setText("Loading location...");
                                                btnRecenter.setEnabled(false);
                                                db.collection("pharmacies").whereEqualTo("ownerId", pId).limit(1).get()
                                                        .addOnSuccessListener(pSnaps -> {
                                                            btnRecenter.setText("Open in Google Maps");
                                                            btnRecenter.setEnabled(true);
                                                            if (!pSnaps.isEmpty()) {
                                                                DocumentSnapshot pDoc = pSnaps.getDocuments().get(0);
                                                                Double lat = pDoc.getDouble("latitude");
                                                                Double lng = pDoc.getDouble("longitude");
                                                                if (lat != null && lng != null) {
                                                                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
                                                                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                                    mapIntent.setPackage("com.google.android.apps.maps");
                                                                    startActivity(mapIntent);
                                                                    return;
                                                                }
                                                            }
                                                            Toast.makeText(LiveMapActivity.this, "Pharmacy location not found", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        }
                                    } else if (status.equalsIgnoreCase("out_for_delivery")) {
                                        String customerId = doc.getString("customerId");
                                        if (customerId != null) {
                                            btnRecenter.setText("Loading location...");
                                            btnRecenter.setEnabled(false);
                                            db.collection("users").document(customerId).get()
                                                    .addOnSuccessListener(cDoc -> {
                                                        btnRecenter.setText("Open in Google Maps");
                                                        btnRecenter.setEnabled(true);
                                                        if (cDoc.exists()) {
                                                            Double lat = cDoc.getDouble("latitude");
                                                            Double lng = cDoc.getDouble("longitude");
                                                            if (lat != null && lng != null) {
                                                                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
                                                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                                mapIntent.setPackage("com.google.android.apps.maps");
                                                                startActivity(mapIntent);
                                                                return;
                                                            }
                                                        }
                                                        // Fallback to text address
                                                        if (addr != null && !addr.isEmpty()) {
                                                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(addr));
                                                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                            mapIntent.setPackage("com.google.android.apps.maps");
                                                            startActivity(mapIntent);
                                                        } else {
                                                            Toast.makeText(LiveMapActivity.this, "Customer location not found", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            if (addr != null && !addr.isEmpty()) {
                                                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(addr));
                                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                mapIntent.setPackage("com.google.android.apps.maps");
                                                startActivity(mapIntent);
                                            } else {
                                                Toast.makeText(LiveMapActivity.this, "Customer address not found", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }

                            break; // show only the first active order
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupButtons() {
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent i = new Intent(this, RiderDashboardActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        View navHistory = findViewById(R.id.navHistory);
        if (navHistory != null) {
            navHistory.setOnClickListener(v ->
                    startActivity(new Intent(this, DeliveryHistoryActivity.class)));
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, RiderProfileActivity.class)));
        }
    }
}
