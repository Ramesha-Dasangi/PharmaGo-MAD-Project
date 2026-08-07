package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeliveryProgressActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String orderId;
    private String currentStatus = "";

    // Header views
    private TextView tvOrderTitle, tvCustomerInfo;

    // Static timeline views
    private ImageView iconStep2;
    private TextView tvStep2Title, tvStep2Time;
    private ImageView iconStep4;
    private TextView tvStep4Title, tvStep4Time;
    private ImageView iconStep5;
    private TextView tvStep5Title;
    private View connectorBeforeDelivery;

    // Dynamic pickup steps container
    private LinearLayout containerPickupSteps;

    // Bottom action button
    private Button btnMarkComplete;

    // Per-pharmacy data (ordered)
    private final LinkedHashMap<String, String> pharmIdToName = new LinkedHashMap<>();
    private final Map<String, double[]> pharmIdToLatLng = new java.util.HashMap<>();
    private List<?> pickedUpPharmacies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_progress);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvOrderTitle   = findViewById(R.id.tvOrderTitle);
        tvCustomerInfo = findViewById(R.id.tvCustomerInfo);

        iconStep2      = findViewById(R.id.iconStep2);
        tvStep2Title   = findViewById(R.id.tvStep2Title);
        tvStep2Time    = findViewById(R.id.tvStep2Time);

        iconStep4      = findViewById(R.id.iconStep4);
        tvStep4Title   = findViewById(R.id.tvStep4Title);
        tvStep4Time    = findViewById(R.id.tvStep4Time);

        iconStep5      = findViewById(R.id.iconStep5);
        tvStep5Title   = findViewById(R.id.tvStep5Title);

        connectorBeforeDelivery = findViewById(R.id.connectorBeforeDelivery);
        containerPickupSteps    = findViewById(R.id.containerPickupSteps);
        btnMarkComplete         = findViewById(R.id.btnMarkComplete);

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) ivBack.setOnClickListener(v -> finish());

        orderId = getIntent().getStringExtra("orderId");
        if (orderId != null && !orderId.isEmpty()) {
            if (tvOrderTitle != null) tvOrderTitle.setText("Order #" + orderId);
            fetchOrderDetails();
        } else {
            Toast.makeText(this, "No order ID provided", Toast.LENGTH_SHORT).show();
        }
    }

    //  Fetch order and build the dynamic pickup step cards
    private void fetchOrderDetails() {
        db.collection("orders").document(orderId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                return;
            }

            currentStatus = doc.getString("status");
            if (currentStatus == null) currentStatus = "assigned";

            // Customer info
            String customerId = doc.getString("customerId");
            String address    = doc.getString("deliveryAddress");
            if (address == null) address = doc.getString("address");
            final String finalAddress = address;

            if (customerId != null) {
                db.collection("users").document(customerId).get().addOnSuccessListener(userDoc -> {
                    if (userDoc.exists() && tvCustomerInfo != null) {
                        String name = userDoc.getString("name");
                        if (name != null) {
                            tvCustomerInfo.setText(name + (finalAddress != null ? " · " + finalAddress : ""));
                        }
                    }
                });
            }

            // Already picked-up pharmacies
            List<?> puList = (List<?>) doc.get("pickedUpPharmacies");
            pickedUpPharmacies = puList != null ? puList : new ArrayList<>();

            // Collect unique pharmacies from items
            List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
            pharmIdToName.clear();

            if (items != null) {
                for (Map<String, Object> item : items) {
                    if (item == null) continue;
                    String pId   = (String) item.get("pharmacyId");
                    String pName = (String) item.get("pharmacyName");
                    if (pId == null || pharmIdToName.containsKey(pId)) continue;
                    if (pName == null || pName.trim().isEmpty()) {
                        pName = "Pharmacy (" + pId.substring(0, Math.min(6, pId.length())) + ")";
                    }
                    pharmIdToName.put(pId, pName.trim());
                }
            }

            // If no items, try legacy single pharmacyName field
            if (pharmIdToName.isEmpty()) {
                String legacyPId   = doc.getString("pharmacyId");
                String legacyPName = doc.getString("pharmacyName");
                if (legacyPId != null) {
                    pharmIdToName.put(legacyPId, legacyPName != null ? legacyPName : "Pharmacy");
                }
            }

            // Try to resolve real pharmacy names from pharmacies collection
            resolvePharmacyNamesAndBuildUI();

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void resolvePharmacyNamesAndBuildUI() {
        if (pharmIdToName.isEmpty()) {
            buildPickupStepCards();
            return;
        }

        // Resolve names + real location via pharmacies collection
        final int[] remaining = {pharmIdToName.size()};
        for (String pId : pharmIdToName.keySet()) {
            db.collection("pharmacies").document(pId).get().addOnSuccessListener(pDoc -> {
                if (pDoc.exists()) {
                    String realName = pDoc.getString("pharmacyName");
                    if (realName == null) realName = pDoc.getString("name");
                    if (realName != null && !realName.trim().isEmpty()) {
                        pharmIdToName.put(pId, realName.trim());
                    }
                    Double lat = pDoc.getDouble("latitude");
                    Double lng = pDoc.getDouble("longitude");
                    if (lat != null && lng != null) {
                        pharmIdToLatLng.put(pId, new double[]{lat, lng});
                    }
                }
                remaining[0]--;
                if (remaining[0] <= 0) buildPickupStepCards();
            }).addOnFailureListener(e -> {
                remaining[0]--;
                if (remaining[0] <= 0) buildPickupStepCards();
            });
        }
    }

    //  Build one card per pharmacy in the dynamic container
    private void buildPickupStepCards() {
        if (containerPickupSteps == null) return;
        containerPickupSteps.removeAllViews();

        int colorGreen  = getColor(R.color.green_accept);
        int colorOrange = getColor(R.color.primary_orange);
        int colorGray   = getColor(R.color.divider_color);

        List<String> pharmIds = new ArrayList<>(pharmIdToName.keySet());
        boolean allPickedUp   = true;
        int firstPendingIdx   = -1;

        for (int i = 0; i < pharmIds.size(); i++) {
            String pId      = pharmIds.get(i);
            boolean pickedUp = pickedUpPharmacies.contains(pId);
            if (!pickedUp && firstPendingIdx == -1) firstPendingIdx = i;
            if (!pickedUp) allPickedUp = false;
        }

        for (int i = 0; i < pharmIds.size(); i++) {
            String pId       = pharmIds.get(i);
            String pName     = pharmIdToName.get(pId);
            boolean pickedUp = pickedUpPharmacies.contains(pId);
            boolean isNext   = (i == firstPendingIdx);

            // Connector line above each step
            View connector = new View(this);
            LinearLayout.LayoutParams connLp = new LinearLayout.LayoutParams(
                    dpToPx(2), dpToPx(20));
            connLp.setMarginStart(dpToPx(13));
            connector.setLayoutParams(connLp);
            connector.setBackgroundColor(pickedUp ? colorGreen : colorGray);
            containerPickupSteps.addView(connector);

            // Step row
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(rowLp);

            // Icon
            ImageView icon = new ImageView(this);
            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28));
            icon.setLayoutParams(iconLp);
            if (pickedUp) {
                icon.setImageResource(android.R.drawable.checkbox_on_background);
                icon.setColorFilter(colorGreen);
            } else if (isNext) {
                icon.setImageResource(android.R.drawable.ic_menu_myplaces);
                icon.setColorFilter(colorOrange);
            } else {
                icon.setImageResource(android.R.drawable.ic_menu_myplaces);
                icon.setColorFilter(colorGray);
            }

            // Text column
            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            textLp.setMarginStart(dpToPx(12));
            textCol.setLayoutParams(textLp);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(pickedUp ? "Picked up from " + pName : "Pickup from " + pName);
            tvTitle.setTextSize(15);
            tvTitle.setTypeface(null, Typeface.BOLD);
            tvTitle.setTextColor(pickedUp ? getColor(R.color.text_primary)
                    : isNext ? colorOrange : colorGray);

            TextView tvSub = new TextView(this);
            tvSub.setTextSize(12);
            tvSub.setTextColor(getColor(R.color.text_secondary));
            tvSub.setText(pickedUp ? "Done ✓" : isNext ? "In progress..." : "Pending");

            textCol.addView(tvTitle);
            textCol.addView(tvSub);
            row.addView(icon);
            row.addView(textCol);

            // Navigate + Confirm pickup buttons — ONLY for the current next pharmacy.
            // Rider must go to this pharmacy's location first; the next pharmacy's
            // Navigate button only appears once this one is confirmed picked up.
            if (isNext && !isOutForDelivery()) {
                double[] latLng = pharmIdToLatLng.get(pId);
                if (latLng != null) {
                    Button btnNavigate = new Button(this);
                    btnNavigate.setText("📍 Navigate");
                    btnNavigate.setTransformationMethod(null);
                    btnNavigate.setTextSize(12);
                    btnNavigate.setTextColor(colorOrange);
                    btnNavigate.setBackgroundResource(R.drawable.bg_card_outline_orange);

                    LinearLayout.LayoutParams navLp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(36));
                    navLp.setMarginStart(dpToPx(8));
                    btnNavigate.setLayoutParams(navLp);

                    final double navLat = latLng[0];
                    final double navLng = latLng[1];
                    final String navLabel = pName;
                    btnNavigate.setOnClickListener(v -> openNavigation(navLat, navLng, navLabel));
                    row.addView(btnNavigate);
                }

                Button btnConfirm = new Button(this);
                btnConfirm.setText("Confirm Pickup");
                btnConfirm.setTransformationMethod(null); // prevent all-caps
                btnConfirm.setTextSize(12);
                btnConfirm.setTextColor(getColor(R.color.white));
                btnConfirm.setBackgroundResource(R.drawable.bg_rounded_orange);

                LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(36));
                btnLp.setMarginStart(dpToPx(8));
                btnConfirm.setLayoutParams(btnLp);

                final String capturedPId = pId;
                btnConfirm.setOnClickListener(v -> confirmPickup(capturedPId, pharmIds));
                row.addView(btnConfirm);
            }

            containerPickupSteps.addView(row);
        }

        // Update bottom delivery steps color
        updateDeliveryStepsUI(allPickedUp);

        // Bottom action button
        updateBottomButton(allPickedUp);
    }

    // Opens turn-by-turn navigation (Google Maps) to a single pharmacy's real location.
    private void openNavigation(double lat, double lng, String label) {
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback: open in browser if Google Maps app isn't installed
                String url = "https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Couldn't open navigation for " + label, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isOutForDelivery() {
        return "out_for_delivery".equalsIgnoreCase(currentStatus)
                || "delivered".equalsIgnoreCase(currentStatus);
    }

    private void confirmPickup(String pharmacyId, List<String> allPharmIds) {
        if (orderId == null) return;

        Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("pickedUpPharmacies", FieldValue.arrayUnion(pharmacyId));

        // Check if this is the last pharmacy
        List<String> updatedPickedUp = new ArrayList<>();
        for (Object o : pickedUpPharmacies) updatedPickedUp.add(o.toString());
        updatedPickedUp.add(pharmacyId);

        boolean allDone = updatedPickedUp.size() >= allPharmIds.size();
        if (allDone) {
            updates.put("status", "out_for_delivery");
        }

        db.collection("orders").document(orderId).update(updates)
                .addOnSuccessListener(unused -> {
                    pickedUpPharmacies = updatedPickedUp;
                    if (allDone) currentStatus = "out_for_delivery";
                    Toast.makeText(this,
                            allDone ? "All pharmacies picked up! Heading to customer." : "Pickup confirmed!",
                            Toast.LENGTH_SHORT).show();
                    buildPickupStepCards();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateDeliveryStepsUI(boolean allPickedUp) {
        int colorGreen  = getColor(R.color.green_accept);
        int colorOrange = getColor(R.color.primary_orange);
        int colorGray   = getColor(R.color.divider_color);
        boolean isDelivered = "delivered".equalsIgnoreCase(currentStatus);

        if (connectorBeforeDelivery != null)
            connectorBeforeDelivery.setBackgroundColor(allPickedUp ? colorGreen : colorGray);

        if (iconStep4 != null) {
            if (isDelivered) {
                iconStep4.setImageResource(android.R.drawable.checkbox_on_background);
                iconStep4.setColorFilter(colorGreen);
            } else if (allPickedUp) {
                iconStep4.setImageResource(android.R.drawable.ic_menu_directions);
                iconStep4.setColorFilter(colorOrange);
            } else {
                iconStep4.setImageResource(android.R.drawable.ic_menu_directions);
                iconStep4.setColorFilter(colorGray);
            }
        }
        if (tvStep4Title != null)
            tvStep4Title.setTextColor(allPickedUp ? (isDelivered ? getColor(R.color.text_primary) : colorOrange) : colorGray);
        if (tvStep4Time != null)
            tvStep4Time.setText(isDelivered ? "Done" : allPickedUp ? "In progress..." : "Pending");

        if (iconStep5 != null) {
            iconStep5.setImageResource(isDelivered ? android.R.drawable.checkbox_on_background : android.R.drawable.ic_menu_view);
            iconStep5.setColorFilter(isDelivered ? colorGreen : colorGray);
        }
        if (tvStep5Title != null)
            tvStep5Title.setTextColor(isDelivered ? getColor(R.color.text_primary) : colorGray);
    }

    private void updateBottomButton(boolean allPickedUp) {
        if (btnMarkComplete == null) return;

        if ("delivered".equalsIgnoreCase(currentStatus)) {
            btnMarkComplete.setText("Delivered ✓");
            btnMarkComplete.setEnabled(false);
            return;
        }

        if (allPickedUp || "out_for_delivery".equalsIgnoreCase(currentStatus)) {
            btnMarkComplete.setText("Mark as Delivered");
            btnMarkComplete.setOnClickListener(v -> {
                Intent intent = new Intent(this, ConfirmDeliveryActivity.class);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            });
        } else {
            btnMarkComplete.setText("Confirm pickups above");
            btnMarkComplete.setEnabled(false);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}