package com.nibm.pharmagomadproject.deliveryrider;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.auth.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class RiderProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvAvatarInitials, tvProfileName, tvProfileVehicle;
    private TextView tvExpandName, tvExpandPhone, tvExpandVehicle;
    private TextView tvProfileTodayEarnings, tvProfileWeekEarnings, tvProfileDeliveries;
    private TextView tvProfileRating;
    private android.widget.LinearLayout layoutReviewsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileVehicle = findViewById(R.id.tvProfileVehicle);
        tvExpandName = findViewById(R.id.tvExpandName);
        tvExpandPhone = findViewById(R.id.tvExpandPhone);
        tvExpandVehicle = findViewById(R.id.tvExpandVehicle);
        tvProfileTodayEarnings = findViewById(R.id.tvProfileTodayEarnings);
        tvProfileWeekEarnings = findViewById(R.id.tvProfileWeekEarnings);
        tvProfileDeliveries = findViewById(R.id.tvProfileDeliveries);
        tvProfileRating = findViewById(R.id.tvProfileRating);
        layoutReviewsList = findViewById(R.id.layoutRiderReviewsList);

        setupExpandableItems();
        setupBottomNav();

        fetchUserData();
        fetchEarnings();
        loadRiderReviews();
    }

    private void fetchUserData() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            // Fetch basic info from users collection
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    String phone = doc.getString("phone");

                    if (name != null) {
                        if (tvProfileName != null) tvProfileName.setText(name);
                        if (tvExpandName != null) tvExpandName.setText(name);

                        // Set Initials
                        String[] parts = name.trim().split("\\s+");
                        String initials = "";
                        if (parts.length > 0) initials += parts[0].charAt(0);
                        if (parts.length > 1) initials += parts[1].charAt(0);
                        if (tvAvatarInitials != null) tvAvatarInitials.setText(initials.toUpperCase());
                    }

                    if (phone != null && tvExpandPhone != null) {
                        tvExpandPhone.setText(phone);
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            });

            // Fetch vehicle & rating info from riders collection
            db.collection("riders").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String vNumber = doc.getString("vehicleReg");
                    String vType = doc.getString("vehicleType");
                    String vehicleStr = (vNumber != null ? vNumber : "Unknown") + " · " + (vType != null ? vType : "Unknown");

                    if (tvProfileVehicle != null) tvProfileVehicle.setText(vehicleStr);
                    if (tvExpandVehicle != null) tvExpandVehicle.setText(vehicleStr);
                }
            });

            // Compute live avg rating from reviews collection
            db.collection("reviews").whereEqualTo("riderId", uid).get().addOnSuccessListener(snap -> {
                if (snap != null && !snap.isEmpty()) {
                    double sum = 0;
                    int count = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot d : snap.getDocuments()) {
                        Double r = d.getDouble("rating");
                        if (r != null) { sum += r; count++; }
                    }
                    if (count > 0) {
                        double avg = sum / count;
                        String ratingStr = String.format(java.util.Locale.getDefault(), "%.1f (%d)", avg, count);
                        if (tvProfileRating != null) tvProfileRating.setText(ratingStr);
                        // Update stored rating in Firestore
                        java.util.Map<String, Object> upd = new java.util.HashMap<>();
                        upd.put("rating", avg);
                        upd.put("ratingCount", (long) count);
                        db.collection("riders").document(uid).update(upd);
                    } else {
                        if (tvProfileRating != null) tvProfileRating.setText("No ratings");
                    }
                } else {
                    if (tvProfileRating != null) tvProfileRating.setText("No ratings");
                }
            });
        }
    }

    private void fetchEarnings() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            db.collection("orders").whereEqualTo("riderId", uid).whereEqualTo("status", "delivered")
                    .get().addOnSuccessListener(querySnapshot -> {
                        long todayEarned = 0;
                        long weekEarned = 0;

                        java.util.Calendar todayCal = java.util.Calendar.getInstance();
                        todayCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                        todayCal.set(java.util.Calendar.MINUTE, 0);
                        todayCal.set(java.util.Calendar.SECOND, 0);
                        todayCal.set(java.util.Calendar.MILLISECOND, 0);
                        long startOfDay = todayCal.getTimeInMillis();

                        java.util.Calendar weekCal = java.util.Calendar.getInstance();
                        weekCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                        weekCal.set(java.util.Calendar.MINUTE, 0);
                        weekCal.set(java.util.Calendar.SECOND, 0);
                        weekCal.set(java.util.Calendar.MILLISECOND, 0);
                        weekCal.add(java.util.Calendar.DAY_OF_YEAR, -7);
                        long startOfWeek = weekCal.getTimeInMillis();

                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Long deliveryFee = doc.getLong("deliveryFee");
                            long fee = deliveryFee != null ? deliveryFee : 100L;

                            Long completedAt = doc.getLong("completedAt");
                            if (completedAt == null) {
                                completedAt = doc.getLong("createdAt");
                            }
                            if (completedAt != null) {
                                if (completedAt >= startOfDay) {
                                    todayEarned += fee;
                                }
                                if (completedAt >= startOfWeek) {
                                    weekEarned += fee;
                                }
                            }
                        }

                        if (tvProfileTodayEarnings != null) tvProfileTodayEarnings.setText("Rs. " + todayEarned);
                        if (tvProfileWeekEarnings != null) tvProfileWeekEarnings.setText("Rs. " + weekEarned);
                        if (tvProfileDeliveries != null) tvProfileDeliveries.setText(String.valueOf(querySnapshot.size()));
                    });
        }
    }

    private void setupExpandableItems() {
        // Personal Info
        setupExpandable(
                R.id.rowPersonalInfo,
                R.id.expandPersonalInfo,
                R.id.chevronPersonal
        );

        // Earnings
        setupExpandable(
                R.id.rowEarnings,
                R.id.expandEarnings,
                R.id.chevronEarnings
        );

        // Notifications
        setupExpandable(
                R.id.rowNotifications,
                R.id.expandNotifications,
                R.id.chevronNotif
        );

        // Customer Reviews
        setupExpandable(
                R.id.rowReviews,
                R.id.expandReviews,
                R.id.chevronReviews
        );

        // Log out
        View itemLogout = findViewById(R.id.itemLogout);
        if (itemLogout != null) {
            itemLogout.setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Notifications switch
        android.widget.Switch switchOrderAlerts = findViewById(R.id.switchOrderAlerts);
        if (switchOrderAlerts != null) {
            android.content.SharedPreferences prefs = getSharedPreferences("RiderPrefs", MODE_PRIVATE);
            switchOrderAlerts.setChecked(prefs.getBoolean("new_order_alerts", true));
            switchOrderAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("new_order_alerts", isChecked).apply();
            });
        }
    }

    /** Toggles the expandable section and rotates the chevron */
    private void setupExpandable(int rowId, int expandId, int chevronId) {
        View row = findViewById(rowId);
        LinearLayout expand = findViewById(expandId);
        ImageView chevron = findViewById(chevronId);

        if (row == null || expand == null || chevron == null) return;

        final boolean[] isExpanded = {false};

        row.setOnClickListener(v -> {
            isExpanded[0] = !isExpanded[0];

            if (isExpanded[0]) {
                expand.setVisibility(View.VISIBLE);
                // Rotate chevron down (90°)
                ObjectAnimator.ofFloat(chevron, "rotation", 0f, 90f)
                        .setDuration(200).start();
            } else {
                expand.setVisibility(View.GONE);
                // Rotate chevron back to right
                ObjectAnimator.ofFloat(chevron, "rotation", 90f, 0f)
                        .setDuration(200).start();
            }
        });
    }

    private void setupBottomNav() {
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent i = new Intent(this, RiderDashboardActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }
        View navMap = findViewById(R.id.navMap);
        if (navMap != null) navMap.setOnClickListener(v ->
                startActivity(new Intent(this, LiveMapActivity.class)));

        View navHistory = findViewById(R.id.navHistory);
        if (navHistory != null) navHistory.setOnClickListener(v ->
                startActivity(new Intent(this, DeliveryHistoryActivity.class)));

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) navProfile.setOnClickListener(v -> { /* already here */ });
    }

    private void loadRiderReviews() {
        if (mAuth.getCurrentUser() == null) return;
        if (layoutReviewsList == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        // NOTE: no .orderBy() here on purpose — whereEqualTo + orderBy on different
        // fields needs a Firestore composite index. We sort client-side instead so
        // this works immediately without any Firebase Console setup.
        db.collection("reviews").whereEqualTo("riderId", uid)
                .get().addOnSuccessListener(snap -> {
                    layoutReviewsList.removeAllViews();
                    if (snap == null || snap.isEmpty()) {
                        TextView tv = new TextView(this);
                        tv.setText("No reviews yet.");
                        tv.setTextColor(0xFF888888);
                        tv.setTextSize(13);
                        tv.setPadding(0, 8, 0, 8);
                        layoutReviewsList.addView(tv);
                        return;
                    }

                    List<com.google.firebase.firestore.DocumentSnapshot> docs = new ArrayList<>(snap.getDocuments());
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
                        View divider = new View(this);
                        android.widget.LinearLayout.LayoutParams dp = new android.widget.LinearLayout.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1);
                        divider.setLayoutParams(dp);
                        divider.setBackgroundColor(0xFFF0F0F0);
                        layoutReviewsList.addView(divider);

                        // Name + stars row
                        android.widget.LinearLayout nameRow = new android.widget.LinearLayout(this);
                        nameRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                        nameRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

                        TextView tvName = new TextView(this);
                        tvName.setText(customerName);
                        tvName.setTextColor(0xFF212121);
                        tvName.setTextSize(14);
                        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
                        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                                0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        tvName.setLayoutParams(lp);

                        TextView tvStars = new TextView(this);
                        StringBuilder stars = new StringBuilder();
                        int r = (rating != null) ? rating.intValue() : 0;
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
                            android.widget.LinearLayout.LayoutParams cLp = new android.widget.LinearLayout.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                            cLp.topMargin = 4;
                            tvComment.setLayoutParams(cLp);
                            row.addView(tvComment);
                        }
                        layoutReviewsList.addView(row);
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}