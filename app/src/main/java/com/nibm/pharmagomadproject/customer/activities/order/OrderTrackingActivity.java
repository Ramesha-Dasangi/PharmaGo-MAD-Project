package com.nibm.pharmagomadproject.customer.activities.order;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.report.ReportIssueActivity;
import com.nibm.pharmagomadproject.customer.activities.review.ReviewActivity;

public class OrderTrackingActivity extends AppCompatActivity {

    private static final String RIDER_PHONE = "0771234567";

    private FirebaseFirestore    db;
    private ListenerRegistration statusListener;
    private MaterialButton       btnReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_tracking);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();

        String orderId    = getIntent().getStringExtra("orderId")    != null ? getIntent()
                .getStringExtra("orderId")    : "";
        String pharmacyId = getIntent().getStringExtra("pharmacyId") != null ? getIntent()
                .getStringExtra("pharmacyId") : "";
        String riderId    = getIntent().getStringExtra("riderId")    != null ? getIntent()
                .getStringExtra("riderId")    : "";

        // Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Report issue (hidden until order is delivered)
        MaterialButton btnReport = findViewById(R.id.btnReportIssue);
        if (btnReport != null) {
            btnReport.setVisibility(View.GONE);
            btnReport.setOnClickListener(v -> {
                Intent i = new Intent(this, ReportIssueActivity.class);
                i.putExtra("orderId", orderId);
                startActivity(i);
            });
        }

        // Review button (hidden until delivered)
        btnReview = findViewById(R.id.btnLeaveReview);
        if (btnReview != null) {
            btnReview.setVisibility(View.GONE);
            btnReview.setOnClickListener(v -> {
                Intent intent = new Intent(this, ReviewActivity.class);
                intent.putExtra("orderId",    orderId);
                intent.putExtra("pharmacyId", pharmacyId);
                intent.putExtra("riderId",    riderId);
                startActivity(intent);
            });
        }

        // Firestore real-time status listener & real data loading
        if (!orderId.isEmpty()) {
            DocumentReference orderRef = db.collection("orders").document(orderId);
            statusListener = orderRef.addSnapshotListener((doc, error) -> {
                if (error != null || doc == null || !doc.exists()) return;
                try {
                    java.util.List<?> items = (java.util.List<?>) doc.get("items");

                    // Load real Order ID header — show full ID
                    android.widget.TextView tvOrderNum = findViewById(R.id.tvOrderNumber);
                    if (tvOrderNum != null) {
                        tvOrderNum.setText("Order #" + doc.getId().toUpperCase());
                    }

                    // Load order summary — structured pharmacy-wise items
                    android.widget.TextView tvOrderSummary = findViewById(R.id.tvOrderSummary);
                    android.widget.LinearLayout layoutSummaryContainer = findViewById(R.id.layoutOrderSummaryContainer);

                    if (tvOrderSummary != null) {
                        tvOrderSummary.setVisibility(View.GONE); // Use container instead of single text
                    }

                    if (layoutSummaryContainer != null) {
                        layoutSummaryContainer.removeAllViews();
                        Object totalObj = doc.get("total");
                        double grandTotal = totalObj instanceof Number ? ((Number) totalObj).doubleValue() : 0.0;
                        Object deliveryFeeObj = doc.get("deliveryFee");
                        double deliveryFee = deliveryFeeObj instanceof Number ? ((Number) deliveryFeeObj).doubleValue() : 50.0;

                        java.util.List<?> confirmedPharmacies = (java.util.List<?>) doc.get("confirmedPharmacies");
                        java.util.List<?> rejectedPharmacies = (java.util.List<?>) doc.get("rejectedPharmacies");

                        // Group items by pharmacy
                        java.util.Map<String, java.util.List<java.util.Map<String, Object>>> pharmacyGroupMap = new java.util.LinkedHashMap<>();
                        java.util.Map<String, String> pharmacyNameMap = new java.util.HashMap<>();

                        if (items != null) {
                            for (Object itemObj : items) {
                                if (!(itemObj instanceof java.util.Map)) continue;
                                java.util.Map<String, Object> itemMap = (java.util.Map<String, Object>) itemObj;
                                String pId = itemMap.get("pharmacyId") != null ? itemMap.get("pharmacyId").toString() : "unknown";
                                String pName = itemMap.get("pharmacyName") != null ? itemMap.get("pharmacyName").toString() : "Pharmacy";

                                pharmacyNameMap.put(pId, pName);
                                if (!pharmacyGroupMap.containsKey(pId)) {
                                    pharmacyGroupMap.put(pId, new java.util.ArrayList<>());
                                }
                                pharmacyGroupMap.get(pId).add(itemMap);
                            }
                        }

                        // Render each pharmacy's block
                        for (java.util.Map.Entry<String, java.util.List<java.util.Map<String, Object>>> entry : pharmacyGroupMap.entrySet()) {
                            String pId = entry.getKey();
                            String pName = pharmacyNameMap.get(pId);
                            java.util.List<java.util.Map<String, Object>> pItems = entry.getValue();

                            android.widget.LinearLayout pharmCard = new android.widget.LinearLayout(this);
                            pharmCard.setOrientation(android.widget.LinearLayout.VERTICAL);
                            pharmCard.setBackgroundResource(R.drawable.bg_rounded_white);
                            pharmCard.setPadding(12, 10, 12, 10);
                            android.widget.LinearLayout.LayoutParams cardLp = new android.widget.LinearLayout.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                            cardLp.bottomMargin = 8;
                            pharmCard.setLayoutParams(cardLp);

                            // Header row with pharmacy name & status badge
                            android.widget.LinearLayout pHeader = new android.widget.LinearLayout(this);
                            pHeader.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                            pHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);

                            android.widget.TextView tvPName = new android.widget.TextView(this);
                            tvPName.setText("🏥 " + pName);
                            tvPName.setTextColor(0xFF1E293B);
                            tvPName.setTextSize(13);
                            tvPName.setTypeface(null, android.graphics.Typeface.BOLD);
                            android.widget.LinearLayout.LayoutParams pLp = new android.widget.LinearLayout.LayoutParams(
                                    0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                            tvPName.setLayoutParams(pLp);
                            pHeader.addView(tvPName);

                            // Status tag for this pharmacy
                            if (confirmedPharmacies != null && confirmedPharmacies.contains(pId)) {
                                android.widget.TextView tag = new android.widget.TextView(this);
                                tag.setText("Approved ✓");
                                tag.setTextColor(0xFF15803D);
                                tag.setTextSize(10);
                                tag.setPadding(8, 2, 8, 2);
                                tag.setBackgroundResource(R.drawable.bg_tag_green);
                                pHeader.addView(tag);
                            } else if (rejectedPharmacies != null && rejectedPharmacies.contains(pId)) {
                                android.widget.TextView tag = new android.widget.TextView(this);
                                tag.setText("Rejected ✗");
                                tag.setTextColor(0xFFB91C1C);
                                tag.setTextSize(10);
                                tag.setPadding(8, 2, 8, 2);
                                tag.setBackgroundResource(R.drawable.bg_circle_red_light);
                                pHeader.addView(tag);
                            }

                            pharmCard.addView(pHeader);

                            // Items list
                            for (java.util.Map<String, Object> itemMap : pItems) {
                                String mName = itemMap.get("medicineName") != null ? itemMap.get("medicineName").toString() : "Item";
                                String qty = itemMap.get("quantity") != null ? itemMap.get("quantity").toString() : "1";
                                Object priceObj = itemMap.get("price");
                                double price = priceObj instanceof Number ? ((Number) priceObj).doubleValue() : 0;

                                android.widget.LinearLayout itemRow = new android.widget.LinearLayout(this);
                                itemRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                                itemRow.setPadding(0, 4, 0, 4);

                                android.widget.TextView tvItem = new android.widget.TextView(this);
                                tvItem.setText("• " + mName + " x" + qty);
                                tvItem.setTextColor(0xFF475569);
                                tvItem.setTextSize(12);
                                android.widget.LinearLayout.LayoutParams iLp = new android.widget.LinearLayout.LayoutParams(
                                        0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                                tvItem.setLayoutParams(iLp);

                                android.widget.TextView tvPrice = new android.widget.TextView(this);
                                tvPrice.setText("Rs. " + String.format("%.0f", price * Double.parseDouble(qty)));
                                tvPrice.setTextColor(0xFF334155);
                                tvPrice.setTextSize(12);

                                itemRow.addView(tvItem);
                                itemRow.addView(tvPrice);
                                pharmCard.addView(itemRow);
                            }

                            layoutSummaryContainer.addView(pharmCard);
                        }

                        // Total breakdown card
                        android.widget.LinearLayout totalCard = new android.widget.LinearLayout(this);
                        totalCard.setOrientation(android.widget.LinearLayout.VERTICAL);
                        totalCard.setPadding(4, 6, 4, 4);

                        android.widget.TextView tvBreakdown = new android.widget.TextView(this);
                        tvBreakdown.setText("Items: Rs. " + String.format("%.0f", grandTotal - deliveryFee)
                                + "  |  Delivery: Rs. " + String.format("%.0f", deliveryFee)
                                + "\nTotal: Rs. " + String.format("%.0f", grandTotal));
                        tvBreakdown.setTextColor(0xFF0F172A);
                        tvBreakdown.setTextSize(12);
                        tvBreakdown.setTypeface(null, android.graphics.Typeface.BOLD);
                        totalCard.addView(tvBreakdown);

                        layoutSummaryContainer.addView(totalCard);
                    }

                    // Rider Info & Card Visibility ONLY show when rider is assigned
                    String rName = doc.getString("riderName");
                    String rPhone = doc.getString("riderPhone");
                    String rId = doc.getString("riderId");
                    boolean hasRider = (rName != null && !rName.trim().isEmpty())
                                    || (rPhone != null && !rPhone.trim().isEmpty())
                                    || (rId != null && !rId.trim().isEmpty());

                    View cardRider = findViewById(R.id.cardRider);
                    if (cardRider != null) {
                        cardRider.setVisibility(hasRider ? View.VISIBLE : View.GONE);
                    }

                    if (hasRider) {
                        android.widget.TextView tvRider = findViewById(R.id.tvRiderName);
                        android.widget.TextView tvRiderRating = findViewById(R.id.tvRiderRating);

                        if (tvRider != null) {
                            tvRider.setText(rName != null && !rName.isEmpty() ? rName + " — your rider" : "Assigned Rider");
                        }
                        ImageView btnCall = findViewById(R.id.btnCallRider);

                        // Load real rider rating and rating count
                        if (rId != null && !rId.isEmpty()) {
                            db.collection("riders").document(rId).get().addOnSuccessListener(rDoc -> {
                                if (rDoc.exists() && tvRiderRating != null) {
                                    Double rating = rDoc.getDouble("rating");
                                    Long count = rDoc.getLong("ratingCount");
                                    if (rating != null && rating > 0) {
                                        String rStr = String.format(java.util.Locale.getDefault(), "⭐ %.1f", rating);
                                        if (count != null && count > 0) {
                                            rStr += " (" + count + " reviews)";
                                        }
                                        tvRiderRating.setText(rStr);
                                    } else {
                                        tvRiderRating.setText("⭐ New Rider");
                                    }
                                }
                            });
                        }

                        if (rPhone != null && !rPhone.isEmpty()) {
                            final String finalPhone = rPhone;
                            if (btnCall != null) {
                                btnCall.setOnClickListener(v -> {
                                    try {
                                        Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + finalPhone));
                                        startActivity(call);
                                    } catch (Exception ignored) {}
                                });
                            }
                        } else if (rId != null && !rId.isEmpty()) {
                            // Fetch rider phone from users collection
                            db.collection("users").document(rId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            String phoneFromDb = userDoc.getString("phone");
                                            String nameFromDb = userDoc.getString("name");
                                            if (tvRider != null && nameFromDb != null) {
                                                tvRider.setText(nameFromDb + " — your rider");
                                            }
                                            if (btnCall != null && phoneFromDb != null && !phoneFromDb.isEmpty()) {
                                                btnCall.setOnClickListener(v -> {
                                                    try {
                                                        Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneFromDb));
                                                        startActivity(call);
                                                    } catch (Exception ignored) {}
                                                });
                                            }
                                        }
                                    });
                        }
                    }

                    // Order Creation Timestamp
                    Object createdAtObj = doc.get("createdAt");
                    String createdTimeStr = formatTime(createdAtObj);
                    if (!createdTimeStr.isEmpty()) {
                        android.widget.TextView tvStep1Time = findViewById(R.id.tvStep1Time);
                        if (tvStep1Time != null) {
                            tvStep1Time.setText("Placed on" + createdTimeStr);
                        }
                    }

                    // Extract per-pharmacy confirmation breakdown for multi-pharmacy orders
                    java.util.List<?> confirmedPharmacies = (java.util.List<?>) doc.get("confirmedPharmacies");
                    java.util.List<?> rejectedPharmacies = (java.util.List<?>) doc.get("rejectedPharmacies");
                    java.util.Map<String, String> pharmMap = new java.util.LinkedHashMap<>();

                    if (items != null) {
                        for (Object itemObj : items) {
                            if (!(itemObj instanceof java.util.Map)) continue;
                            java.util.Map<?, ?> item = (java.util.Map<?, ?>) itemObj;
                            String pId = item.get("pharmacyId") != null ? item.get("pharmacyId").toString() : "";
                            String pName = item.get("pharmacyName") != null ? item.get("pharmacyName").toString() : "Pharmacy";
                            if (!pId.isEmpty()) {
                                pharmMap.put(pId, pName);
                            }
                        }
                    }

                    // Check for rejected pharmacies and append to tvOrderSummary
                    if (tvOrderSummary != null && rejectedPharmacies != null && !rejectedPharmacies.isEmpty()) {
                        StringBuilder rejSb = new StringBuilder();
                        for (Object rIdObj : rejectedPharmacies) {
                            if (rIdObj == null) continue;
                            String rejPharmId = rIdObj.toString();
                            String rejPharmName = pharmMap.get(rejPharmId);
                            if (rejPharmName == null) rejPharmName = "Pharmacy";
                            if (rejSb.length() > 0) rejSb.append(", ");
                            rejSb.append(rejPharmName);
                        }
                        if (rejSb.length() > 0) {
                            String currSummary = tvOrderSummary.getText() != null ? tvOrderSummary.getText().toString() : "";
                            tvOrderSummary.setText(currSummary + "\n⚠️ Rejected Items from: " + rejSb.toString());
                        }
                    }

                    String status = doc.getString("status");
                    
                    // Collect step timestamps
                    java.util.Map<String, String> timeMap = new java.util.HashMap<>();
                    timeMap.put("created", createdTimeStr);
                    timeMap.put("confirmed", formatTime(doc.get("confirmedAt") != null ? doc.get("confirmedAt") : doc.get("approvedAt")));
                    timeMap.put("picked_up", formatTime(doc.get("pickedUpAt") != null ? doc.get("pickedUpAt") : doc.get("assignedAt")));
                    timeMap.put("out_for_delivery", formatTime(doc.get("outForDeliveryAt")));
                    timeMap.put("delivered", formatTime(doc.get("deliveredAt") != null ? doc.get("deliveredAt") : doc.get("completedAt")));

                    updateStatusUI(status, timeMap, pharmMap, confirmedPharmacies, rejectedPharmacies);

                    // Show report & review buttons ONLY when delivered
                    boolean isDelivered = "delivered".equalsIgnoreCase(status);
                    if (btnReview != null) btnReview.setVisibility(isDelivered ? View.VISIBLE : View.GONE);
                    if (btnReport != null) btnReport.setVisibility(isDelivered ? View.VISIBLE : View.GONE);
                } catch (Exception e) {
                    android.util.Log.e("OrderTrackingActivity", "Error loading tracking data: " + e.getMessage(), e);
                }
            });
        }
    }

    private static String formatTime(Object tsObj) {
        if (tsObj == null) return "";
        java.util.Date dateObj = null;
        if (tsObj instanceof com.google.firebase.Timestamp) {
            dateObj = ((com.google.firebase.Timestamp) tsObj).toDate();
        } else if (tsObj instanceof Number) {
            dateObj = new java.util.Date(((Number) tsObj).longValue());
        }
        if (dateObj == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM · hh:mm a", java.util.Locale.getDefault());
        return " · " + sdf.format(dateObj);
    }

    private void updateStatusUI(String status, java.util.Map<String, String> timeMap,
                                java.util.Map<String, String> pharmMap,
                                java.util.List<?> confirmedPharmacies,
                                java.util.List<?> rejectedPharmacies) {
        if (status == null) status = "pending";

        try {
            android.widget.TextView tvStatus = findViewById(R.id.tvCurrentStatus);
            android.widget.TextView tvBadge = findViewById(R.id.tvOrderBadge);

            if (tvBadge != null) {
                tvBadge.setText(status.substring(0, 1).toUpperCase() + status.substring(1).replace("_", " "));
            }

            android.widget.TextView tvStep2Sub = findViewById(R.id.tvStep2Sub);
            android.widget.TextView tvStep3Sub = findViewById(R.id.tvStep3Sub);
            android.widget.TextView tvStep4Sub = findViewById(R.id.tvStep4Sub);
            android.widget.TextView tvStep5Sub = findViewById(R.id.tvStep5Sub);

            String tConfirmed = timeMap.getOrDefault("confirmed", "");
            String tPickedUp = timeMap.getOrDefault("picked_up", "");
            String tOutForDelivery = timeMap.getOrDefault("out_for_delivery", "");
            String tDelivered = timeMap.getOrDefault("delivered", "");

            // Format per-pharmacy status if multi-pharmacy
            String pharmBreakdown = "";
            if (pharmMap != null && pharmMap.size() > 1) {
                StringBuilder sb = new StringBuilder();
                for (java.util.Map.Entry<String, String> entry : pharmMap.entrySet()) {
                    String pId = entry.getKey();
                    String pName = entry.getValue();
                    if (sb.length() > 0) sb.append("\n");
                    sb.append("• ").append(pName).append(": ");
                    if (confirmedPharmacies != null && confirmedPharmacies.contains(pId)) {
                        sb.append("Confirmed ✓");
                    } else if (rejectedPharmacies != null && rejectedPharmacies.contains(pId)) {
                        sb.append("Rejected ✗");
                    } else {
                        sb.append("Awaiting...");
                    }
                }
                pharmBreakdown = sb.toString();
            }

            // Build pharmacy names list for pickup step
            String headingPharmNames = "";
            if (pharmMap != null && !pharmMap.isEmpty()) {
                StringBuilder hSb = new StringBuilder();
                for (String pName : pharmMap.values()) {
                    if (pName == null || pName.isEmpty()) continue;
                    if (hSb.length() > 0) hSb.append(" & ");
                    hSb.append(pName);
                }
                headingPharmNames = hSb.toString();
            }
            if (headingPharmNames.isEmpty()) headingPharmNames = "pharmacy";

            switch (status.toLowerCase()) {
                case "pending":
                case "awaiting_approval":
                    if (tvStatus != null) tvStatus.setText("Order Placed — Waiting for Pharmacy Confirmation");
                    if (tvStep2Sub != null) {
                        tvStep2Sub.setText(!pharmBreakdown.isEmpty() ? pharmBreakdown : "Awaiting confirmation...");
                    }
                    if (tvStep3Sub != null) tvStep3Sub.setText("Pending");
                    if (tvStep4Sub != null) tvStep4Sub.setText("Pending");
                    if (tvStep5Sub != null) tvStep5Sub.setText("Pending");
                    setStepsProgress(1);
                    break;
                case "confirmed":
                case "approved":
                case "processing":
                case "ready":
                case "partially_approved":
                    if (tvStatus != null) tvStatus.setText("Pharmacy is preparing your order");
                    if (tvStep2Sub != null) tvStep2Sub.setText(!pharmBreakdown.isEmpty() ? pharmBreakdown : "Confirmed & Preparing" + tConfirmed);
                    if (tvStep3Sub != null) tvStep3Sub.setText("Awaiting rider assignment...");
                    if (tvStep4Sub != null) tvStep4Sub.setText("Pending");
                    if (tvStep5Sub != null) tvStep5Sub.setText("Pending");
                    setStepsProgress(2);
                    break;
                case "assigned":
                    if (tvStatus != null) tvStatus.setText("Rider assigned — Heading to " + headingPharmNames);
                    if (tvStep2Sub != null) tvStep2Sub.setText(!pharmBreakdown.isEmpty() ? pharmBreakdown : "Confirmed & Prepared" + tConfirmed);
                    if (tvStep3Sub != null) tvStep3Sub.setText("Heading to " + headingPharmNames + tPickedUp);
                    if (tvStep4Sub != null) tvStep4Sub.setText("Pending");
                    if (tvStep5Sub != null) tvStep5Sub.setText("Pending");
                    setStepsProgress(2);
                    break;
                case "picked_up":
                    if (tvStatus != null) tvStatus.setText("Rider picking up from " + headingPharmNames);
                    if (tvStep2Sub != null) tvStep2Sub.setText(!pharmBreakdown.isEmpty() ? pharmBreakdown : "Confirmed & Prepared" + tConfirmed);
                    if (tvStep3Sub != null) tvStep3Sub.setText("Heading to " + headingPharmNames + tPickedUp);
                    if (tvStep4Sub != null) tvStep4Sub.setText("In transit...");
                    if (tvStep5Sub != null) tvStep5Sub.setText("Pending");
                    setStepsProgress(3);
                    break;
                case "out_for_delivery":
                    if (tvStatus != null) tvStatus.setText("Out for delivery");
                    if (tvStep2Sub != null) tvStep2Sub.setText(!pharmBreakdown.isEmpty() ? pharmBreakdown : "Confirmed & Prepared" + tConfirmed);
                    if (tvStep3Sub != null) tvStep3Sub.setText("Picked up by Rider" + tPickedUp);
                    if (tvStep4Sub != null) tvStep4Sub.setText("Out for delivery" + tOutForDelivery);
                    if (tvStep5Sub != null) tvStep5Sub.setText("Arriving soon");
                    setStepsProgress(4);
                    break;
                case "delivered":
                    if (tvStatus != null) tvStatus.setText("Delivered ✓");
                    if (tvStep2Sub != null) tvStep2Sub.setText(!pharmBreakdown.isEmpty() ? pharmBreakdown : "Confirmed" + tConfirmed);
                    if (tvStep3Sub != null) tvStep3Sub.setText("Picked up" + tPickedUp);
                    if (tvStep4Sub != null) tvStep4Sub.setText("Completed" + tOutForDelivery);
                    if (tvStep5Sub != null) tvStep5Sub.setText("Delivered successfully" + tDelivered);
                    setStepsProgress(5);
                    break;
                case "approved_pending_payment":
                    if (tvStatus != null) tvStatus.setText("Approved — Payment Required");
                    if (tvStep2Sub != null) tvStep2Sub.setText(!pharmBreakdown.isEmpty() ? pharmBreakdown : "Pharmacy approved ✓" + tConfirmed);
                    if (tvStep3Sub != null) tvStep3Sub.setText("Awaiting your payment...");
                    if (tvStep4Sub != null) tvStep4Sub.setText("Pending");
                    if (tvStep5Sub != null) tvStep5Sub.setText("Pending");
                    setStepsProgress(2);
                    break;
                case "partially_rejected":
                    if (tvStatus != null) tvStatus.setText("Partially Rejected by Pharmacy ⚠️");
                    if (tvStep2Sub != null) tvStep2Sub.setText(!pharmBreakdown.isEmpty() ? pharmBreakdown : "Some items were rejected");
                    if (tvStep3Sub != null) tvStep3Sub.setText("Awaiting rider assignment...");
                    if (tvStep4Sub != null) tvStep4Sub.setText("Pending");
                    if (tvStep5Sub != null) tvStep5Sub.setText("Pending");
                    setStepsProgress(2);
                    break;
                case "cancelled":
                case "rejected":
                    if (tvStatus != null) tvStatus.setText("Order Cancelled ✗");
                    if (tvStep2Sub != null) tvStep2Sub.setText(!pharmBreakdown.isEmpty() ? pharmBreakdown : "Cancelled");
                    if (tvStep3Sub != null) tvStep3Sub.setText("—");
                    if (tvStep4Sub != null) tvStep4Sub.setText("—");
                    if (tvStep5Sub != null) tvStep5Sub.setText("—");
                    setStepsProgress(0);
                    break;
                default:
                    if (tvStatus != null) tvStatus.setText(status);
            }
        } catch (Exception ignored) {}
    }

    private void setStepsProgress(int activeStep) {
        int[] indicatorIds = {
                R.id.indicatorStep1,
                R.id.indicatorStep2,
                R.id.indicatorStep3,
                R.id.indicatorStep4,
                R.id.indicatorStep5
        };
        int[] iconIds = {
                R.id.iconStep1,
                R.id.iconStep2,
                R.id.iconStep3,
                R.id.iconStep4,
                R.id.iconStep5
        };

        for (int i = 0; i < 5; i++) {
            View indicator = findViewById(indicatorIds[i]);
            ImageView icon = findViewById(iconIds[i]);
            if (indicator == null) continue;

            int stepNum = i + 1;
            if (activeStep == 5 || stepNum < activeStep) {
                // Done step
                indicator.setBackgroundResource(R.drawable.bg_step_done);
                if (icon != null) {
                    icon.setVisibility(View.VISIBLE);
                    icon.setImageTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.pg_primary, null)));
                }
            } else if (stepNum == activeStep) {
                // Active step
                indicator.setBackgroundResource(R.drawable.bg_step_active);
                if (icon != null) {
                    icon.setVisibility(View.VISIBLE);
                    icon.setImageTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(android.R.color.white, null)));
                }
            } else {
                // Pending step
                indicator.setBackgroundResource(R.drawable.bg_step_pending);
                if (icon != null) {
                    icon.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusListener != null) statusListener.remove();
    }
}
