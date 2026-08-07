package com.nibm.pharmagomadproject.pharmacyowner.profile;

import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.auth.LoginActivity;
import com.nibm.pharmagomadproject.pharmacyowner.DashboardActivity;
import com.nibm.pharmagomadproject.pharmacyowner.InventoryActivity;
import com.nibm.pharmagomadproject.pharmacyowner.OrdersActivity;
import com.nibm.pharmagomadproject.pharmacyowner.reports.SalesReportActivity;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private Switch switchNotification;
    private LinearLayout txtChangePassword;
    private LinearLayout txtLogout;

    // Profile display views
    private TextView txtName;
    private TextView txtLicense;
    private TextView txtAddress;
    private TextView txtHours;
    private TextView txtPharmacyRating;
    private TextView txtPharmacyRatingCount;
    private LinearLayout layoutPharmacyReviewsList;
    private LinearLayout expandPharmacyReviews;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String pharmacyDocId; // Firestore document ID for the pharmacy

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase init
        db    = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        bottomNavigation   = findViewById(R.id.bottomNavigation);
        switchNotification = findViewById(R.id.switchNotification);
        txtChangePassword  = findViewById(R.id.txtChangePassword);
        txtLogout          = findViewById(R.id.txtLogout);

        // Profile info views
        txtName    = findViewById(R.id.txtName);
        txtLicense = findViewById(R.id.txtLicense);
        txtAddress = findViewById(R.id.txtAddress);
        txtHours   = findViewById(R.id.txtHours);

        // Rating & Reviews views
        txtPharmacyRating      = findViewById(R.id.txtPharmacyRating);
        txtPharmacyRatingCount = findViewById(R.id.txtPharmacyRatingCount);
        layoutPharmacyReviewsList = findViewById(R.id.layoutPharmacyReviewsList);
        expandPharmacyReviews  = findViewById(R.id.expandPharmacyReviews);

        // Reviews expand/collapse toggle
        LinearLayout rowPharmacyReviews = findViewById(R.id.rowPharmacyReviews);
        if (rowPharmacyReviews != null && expandPharmacyReviews != null) {
            rowPharmacyReviews.setOnClickListener(v -> {
                boolean visible = expandPharmacyReviews.getVisibility() == android.view.View.VISIBLE;
                expandPharmacyReviews.setVisibility(visible ? android.view.View.GONE : android.view.View.VISIBLE);
            });
        }

        // Load real pharmacy data from Firestore
        loadPharmacyProfile();

        // Notification Switch SharedPreferences persistence
        SharedPreferences prefs = getSharedPreferences("PharmaPrefs", MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        switchNotification.setChecked(notificationsEnabled);

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            if (isChecked) {
                Toast.makeText(ProfileActivity.this, "Notifications Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProfileActivity.this, "Notifications Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Change Password
        txtChangePassword.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class)));

        // Logout
        txtLogout.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Bottom Navigation
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(ProfileActivity.this, OrdersActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_inventory) {
                startActivity(new Intent(ProfileActivity.this, InventoryActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(ProfileActivity.this, SalesReportActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    //  Load real pharmacy profile from Firestore
    private void loadPharmacyProfile() {
        com.google.firebase.auth.FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        db.collection("pharmacies")
                .whereEqualTo("ownerId", uid)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots == null || snapshots.isEmpty()) return;

                    com.google.firebase.firestore.DocumentSnapshot doc =
                            snapshots.getDocuments().get(0);
                    pharmacyDocId = doc.getId();

                    // Pharmacy name
                    String name = doc.getString("name");
                    if (txtName != null && name != null && !name.isEmpty()) {
                        txtName.setText(name);
                    }

                    // License number
                    String licenseNo = doc.getString("licenseNo");
                    if (txtLicense != null) {
                        if (licenseNo != null && !licenseNo.isEmpty()) {
                            txtLicense.setText("License : " + licenseNo);
                        } else {
                            txtLicense.setText("License : —");
                        }
                    }

                    // Address
                    String address = doc.getString("address");
                    if (txtAddress != null) {
                        if (address != null && !address.isEmpty()) {
                            txtAddress.setText(address);
                        } else {
                            txtAddress.setText("—");
                        }
                    }

                    // Operating hours (no field in schema — keep default or show owner email)
                    String hours = doc.getString("hours");
                    if (txtHours != null && hours != null && !hours.isEmpty()) {
                        txtHours.setText(hours);
                    }
                    // else: leaves the default XML text "8:00 AM - 10:00 PM"

                    // Rating from Firestore
                    Double rating = doc.getDouble("rating");
                    Long ratingCount = doc.getLong("ratingCount");
                    if (txtPharmacyRating != null) {
                        if (rating != null && rating > 0) {
                            txtPharmacyRating.setText(String.format(java.util.Locale.getDefault(), "★  %.1f", rating));
                        } else {
                            txtPharmacyRating.setText("★  —");
                        }
                    }
                    if (txtPharmacyRatingCount != null) {
                        if (ratingCount != null && ratingCount > 0) {
                            txtPharmacyRatingCount.setText("(" + ratingCount + " reviews)");
                        } else {
                            txtPharmacyRatingCount.setText("No reviews yet");
                        }
                    }

                    // Load reviews for this pharmacy.
                    loadPharmacyReviews(uid);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void loadPharmacyReviews(String ownerUid) {
        if (ownerUid == null || ownerUid.isEmpty() || layoutPharmacyReviewsList == null) return;

        db.collection("reviews").whereEqualTo("pharmacyId", ownerUid)
                .get().addOnSuccessListener(snap -> {
                    layoutPharmacyReviewsList.removeAllViews();

                    // Live count update — update ratingCount and average rating from reviews
                    if (snap != null && !snap.isEmpty()) {
                        int count = snap.size();
                        double sum = 0;
                        for (com.google.firebase.firestore.DocumentSnapshot d : snap.getDocuments()) {
                            Long r = d.getLong("rating");
                            if (r != null) sum += r;
                        }
                        double avg = count > 0 ? sum / count : 0;
                        // Update Firestore pharmacy doc with real count
                        if (pharmacyDocId != null && !pharmacyDocId.isEmpty()) {
                            java.util.Map<String, Object> upd = new java.util.HashMap<>();
                            upd.put("ratingCount", (long) count);
                            upd.put("rating", avg);
                            db.collection("pharmacies").document(pharmacyDocId).update(upd);
                        }
                        // Update UI immediately
                        if (txtPharmacyRating != null)
                            txtPharmacyRating.setText(String.format(java.util.Locale.getDefault(), "★  %.1f", avg));
                        if (txtPharmacyRatingCount != null)
                            txtPharmacyRatingCount.setText("(" + count + " reviews)");
                    } else {
                        if (txtPharmacyRatingCount != null)
                            txtPharmacyRatingCount.setText("No reviews yet");
                    }

                    if (snap == null || snap.isEmpty()) {
                        TextView tv = new TextView(this);
                        tv.setText("No reviews yet.");
                        tv.setTextColor(0xFF888888);
                        tv.setTextSize(13);
                        tv.setPadding(0, 8, 0, 8);
                        layoutPharmacyReviewsList.addView(tv);
                        return;
                    }

                    java.util.List<com.google.firebase.firestore.DocumentSnapshot> docs =
                            new java.util.ArrayList<>(snap.getDocuments());
                    docs.sort((a, b) -> {
                        Long ta = a.getLong("createdAt");
                        Long tb = b.getLong("createdAt");
                        long va = ta != null ? ta : 0L;
                        long vb = tb != null ? tb : 0L;
                        return Long.compare(vb, va); // descending — newest first
                    });

                    for (com.google.firebase.firestore.DocumentSnapshot d : docs) {
                        String customerName = d.getString("customerName");
                        if (customerName == null || customerName.isEmpty()) customerName = "Customer";
                        String reviewOrderId = d.getString("orderId");
                        Long rating = d.getLong("rating");
                        String comment = d.getString("comment");

                        android.widget.LinearLayout row = new android.widget.LinearLayout(this);
                        row.setOrientation(android.widget.LinearLayout.VERTICAL);
                        row.setPadding(0, 12, 0, 12);

                        // Divider
                        android.view.View divider = new android.view.View(this);
                        android.widget.LinearLayout.LayoutParams divLp = new android.widget.LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1);
                        divider.setLayoutParams(divLp);
                        divider.setBackgroundColor(0xFFEEEEEE);
                        layoutPharmacyReviewsList.addView(divider);

                        // Name + stars
                        android.widget.LinearLayout nameRow = new android.widget.LinearLayout(this);
                        nameRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                        nameRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

                        TextView tvName = new TextView(this);
                        tvName.setText(customerName);
                        tvName.setTextColor(0xFF212121);
                        tvName.setTextSize(14);
                        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
                        android.widget.LinearLayout.LayoutParams nlp = new android.widget.LinearLayout.LayoutParams(
                                0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        tvName.setLayoutParams(nlp);

                        TextView tvStars = new TextView(this);
                        StringBuilder stars = new StringBuilder();
                        int r = rating != null ? rating.intValue() : 0;
                        for (int i = 0; i < 5; i++) stars.append(i < r ? "★" : "☆");
                        tvStars.setText(stars.toString());
                        tvStars.setTextColor(0xFFFFC107);
                        tvStars.setTextSize(15);

                        nameRow.addView(tvName);
                        nameRow.addView(tvStars);
                        row.addView(nameRow);

                        // Order ID subtitle
                        if (reviewOrderId != null && !reviewOrderId.isEmpty()) {
                            TextView tvOrderRef = new TextView(this);
                            tvOrderRef.setText("Order #" + reviewOrderId.toUpperCase());
                            tvOrderRef.setTextColor(0xFF9E9E9E);
                            tvOrderRef.setTextSize(11);
                            android.widget.LinearLayout.LayoutParams oidLp = new android.widget.LinearLayout.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                            oidLp.topMargin = 2;
                            tvOrderRef.setLayoutParams(oidLp);
                            row.addView(tvOrderRef);
                        }

                        if (comment != null && !comment.isEmpty()) {
                            TextView tvComment = new TextView(this);
                            tvComment.setText(comment);
                            tvComment.setTextColor(0xFF555555);
                            tvComment.setTextSize(13);
                            android.widget.LinearLayout.LayoutParams clp = new android.widget.LinearLayout.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                            clp.topMargin = 4;
                            tvComment.setLayoutParams(clp);
                            row.addView(tvComment);
                        }
                        layoutPharmacyReviewsList.addView(row);
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}