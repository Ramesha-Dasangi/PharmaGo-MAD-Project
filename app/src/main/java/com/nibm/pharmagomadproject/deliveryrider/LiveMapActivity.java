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

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import android.preference.PreferenceManager;

import java.util.List;
import java.util.Map;

public class LiveMapActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        setContentView(R.layout.activity_live_map);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        MapView map = findViewById(R.id.mapView);
        if (map != null) {
            map.setMultiTouchControls(true);
            map.getController().setZoom(15.0);
            map.getController().setCenter(new GeoPoint(6.9271, 79.8612));
        }

        setupButtons();
        loadActiveOrderDetails();
    }

    private void setupMapLocation(Double lat, Double lng, String title) {
        MapView map = findViewById(R.id.mapView);
        if (map != null && lat != null && lng != null) {
            GeoPoint point = new GeoPoint(lat, lng);
            map.getController().setCenter(point);
            Marker marker = new Marker(map);
            marker.setPosition(point);
            marker.setTitle(title);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(marker);
            map.invalidate();
        }
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
                                List<String> pIds = (List<String>) doc.get("pharmacyIds");
                                if (pIds != null && pIds.size() > 1) {
                                    if (tvDropoffLabel != null) tvDropoffLabel.setText("Pickup from");
                                    if (tvDropoffValue != null) tvDropoffValue.setText("Multiple Pharmacies (" + pIds.size() + ")");
                                } else {
                                    if (tvDropoffLabel != null) tvDropoffLabel.setText("Pickup from");
                                    if (tvDropoffValue != null) tvDropoffValue.setText(pharmName != null ? pharmName : "Pharmacy");
                                }
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
                                if (status.equalsIgnoreCase("assigned") || status.equalsIgnoreCase("picked_up")) {
                                    List<String> pIdsList = (List<String>) doc.get("pharmacyIds");
                                    if (pIdsList != null && pIdsList.size() > 1) {
                                        db.collection("pharmacies").whereIn("ownerId", pIdsList).get()
                                                .addOnSuccessListener(pSnaps -> {
                                                    if (!pSnaps.isEmpty()) {
                                                        StringBuilder waypoints = new StringBuilder();
                                                        String destination = "";
                                                        int count = 0;
                                                        for (DocumentSnapshot pDoc : pSnaps.getDocuments()) {
                                                            Double lat = pDoc.getDouble("latitude");
                                                            Double lng = pDoc.getDouble("longitude");
                                                            String pName = pDoc.getString("name");
                                                            if (lat != null && lng != null) {
                                                                setupMapLocation(lat, lng, pName != null ? pName : "Pharmacy");
                                                                if (count == pSnaps.size() - 1) {
                                                                    destination = lat + "," + lng;
                                                                } else {
                                                                    if (waypoints.length() > 0) waypoints.append("|");
                                                                    waypoints.append(lat).append(",").append(lng);
                                                                }
                                                                count++;
                                                            }
                                                        }
                                                        final String fDestination = destination;
                                                        final String fWaypoints = waypoints.toString();
                                                        btnRecenter.setOnClickListener(v -> {
                                                            if (!fDestination.isEmpty()) {
                                                                String url = "https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(fDestination);
                                                                if (fWaypoints.length() > 0) {
                                                                    url += "&waypoints=" + Uri.encode(fWaypoints);
                                                                }
                                                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                                mapIntent.setPackage("com.google.android.apps.maps");
                                                                startActivity(mapIntent);
                                                            }
                                                        });
                                                    }
                                                });
                                    } else {
                                        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                                        if (items != null && !items.isEmpty()) {
                                            String pId = (String) items.get(0).get("pharmacyId");
                                            if (pId != null) {
                                                db.collection("pharmacies").whereEqualTo("ownerId", pId).limit(1).get()
                                                        .addOnSuccessListener(pSnaps -> {
                                                            if (!pSnaps.isEmpty()) {
                                                                DocumentSnapshot pDoc = pSnaps.getDocuments().get(0);
                                                                Double lat = pDoc.getDouble("latitude");
                                                                Double lng = pDoc.getDouble("longitude");
                                                                String pName = pDoc.getString("name");
                                                                if (lat != null && lng != null) {
                                                                    setupMapLocation(lat, lng, pName != null ? pName : "Pharmacy");
                                                                    btnRecenter.setOnClickListener(v -> {
                                                                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
                                                                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                                        mapIntent.setPackage("com.google.android.apps.maps");
                                                                        startActivity(mapIntent);
                                                                    });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                } else if (status.equalsIgnoreCase("out_for_delivery")) {
                                    String customerId = doc.getString("customerId");
                                    if (customerId != null) {
                                        db.collection("users").document(customerId).get()
                                                .addOnSuccessListener(cDoc -> {
                                                    if (cDoc.exists()) {
                                                        Double lat = cDoc.getDouble("latitude");
                                                        Double lng = cDoc.getDouble("longitude");
                                                        if (lat != null && lng != null) {
                                                            setupMapLocation(lat, lng, "Customer");
                                                            btnRecenter.setOnClickListener(v -> {
                                                                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
                                                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                                mapIntent.setPackage("com.google.android.apps.maps");
                                                                startActivity(mapIntent);
                                                            });
                                                            return;
                                                        }
                                                    }
                                                    // Fallback
                                                    btnRecenter.setOnClickListener(v -> {
                                                        if (addr != null && !addr.isEmpty()) {
                                                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(addr));
                                                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                            mapIntent.setPackage("com.google.android.apps.maps");
                                                            startActivity(mapIntent);
                                                        }
                                                    });
                                                });
                                    } else {
                                        btnRecenter.setOnClickListener(v -> {
                                            if (addr != null && !addr.isEmpty()) {
                                                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(addr));
                                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                                mapIntent.setPackage("com.google.android.apps.maps");
                                                startActivity(mapIntent);
                                            }
                                        });
                                    }
                                }
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
