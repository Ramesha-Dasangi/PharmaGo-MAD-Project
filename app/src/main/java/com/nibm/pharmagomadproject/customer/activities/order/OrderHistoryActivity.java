package com.nibm.pharmagomadproject.customer.activities.order;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.report.ReportIssueActivity;
import com.nibm.pharmagomadproject.customer.activities.home.HomeActivity;
import com.nibm.pharmagomadproject.customer.activities.medicine.MedicineListActivity;
import com.nibm.pharmagomadproject.customer.activities.profile.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private TextView tabAll, tabActive, tabDelivered, tabCancelled;
    private String currentTab = "all";

    // Sample orders — Firebase ekata connect karaddi real data ganna
    static class Order {
        String id, date, pharmacy, status, amount;
        Order(String id, String date, String pharmacy, String status, String amount) {
            this.id=id; this.date=date; this.pharmacy=pharmacy;
            this.status=status; this.amount=amount;
        }
    }

    private final List<Order> allOrders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        tabAll       = findViewById(R.id.tabAll);
        tabActive    = findViewById(R.id.tabActive);
        tabDelivered = findViewById(R.id.tabDelivered);
        tabCancelled = findViewById(R.id.tabCancelled);

        // Load sample orders
        allOrders.add(new Order("#PG-00234", "21 Jun · 2 pharmacies", "MediCare + City Pharma", "active",    "Rs. 304"));
        allOrders.add(new Order("#PG-00229", "19 Jun",                "MediCare Pharmacy",      "delivered", "Rs. 156"));
        allOrders.add(new Order("#PG-00211", "15 Jun",                "City Pharma",            "delivered", "Rs. 890"));
        allOrders.add(new Order("#PG-00198", "12 Jun",                "MediCare Pharmacy",      "cancelled", "Rs. 240"));

        // Tab clicks
        tabAll.setOnClickListener(v       -> { currentTab = "all";       selectTab(tabAll);       renderOrders(); });
        tabActive.setOnClickListener(v    -> { currentTab = "active";    selectTab(tabActive);    renderOrders(); });
        tabDelivered.setOnClickListener(v -> { currentTab = "delivered"; selectTab(tabDelivered); renderOrders(); });
        tabCancelled.setOnClickListener(v -> { currentTab = "cancelled"; selectTab(tabCancelled); renderOrders(); });

        renderOrders(); // show all by default

        setupBottomNav();
    }

    private void selectTab(TextView selected) {
        TextView[] tabs = {tabAll, tabActive, tabDelivered, tabCancelled};
        int primary = getResources().getColor(R.color.pg_primary, null);
        int sub     = getResources().getColor(R.color.pg_sub, null);
        for (TextView tab : tabs) {
            boolean sel = tab == selected;
            tab.setBackgroundResource(sel ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
            tab.setTextColor(sel ? primary : sub);
            tab.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    private void renderOrders() {
        // Get the ScrollView
        ScrollView scrollView = findViewById(R.id.order_scroll); // add id in XML if missing

        if (scrollView != null) {
            // Get the LinearLayout inside ScrollView
            LinearLayout container = scrollView.findViewById(R.id.order_container);

            if (container != null) {
                // Example: add a TextView dynamically
                TextView tv = new TextView(this);
                tv.setText("New Order Item");
                container.addView(tv);
            }
        }

        // Use a different approach — get the scroll's child directly
        android.widget.ScrollView sv = findScrollView();
        if (sv == null) return;
        LinearLayout list = (LinearLayout) sv.getChildAt(0);
        list.removeAllViews();

        List<Order> filtered = new ArrayList<>();
        for (Order o : allOrders) {
            if ("all".equals(currentTab) || o.status.equals(currentTab)) {
                filtered.add(o);
            }
        }

        if (filtered.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No " + currentTab + " orders");
            empty.setTextColor(getResources().getColor(R.color.pg_sub, null));
            empty.setTextSize(13);
            empty.setPadding(0, 48, 0, 0);
            empty.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            list.addView(empty, lp);
            return;
        }

        for (Order order : filtered) {
            addOrderCard(list, order);
        }
    }

    private void addOrderCard(LinearLayout parent, Order order) {
        // Build card programmatically to avoid RecyclerView dependency
        CardView card = new CardView(this);
        CardView.LayoutParams lp = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        card.setLayoutParams(lp);
        card.setRadius(dp(12));
        card.setCardElevation(dp(2));
        card.setCardBackgroundColor(getResources().getColor(R.color.pg_card, null));

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(12), dp(12), dp(12), dp(12));

        // Row 1: order ID + status badge
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvId = new TextView(this);
        tvId.setText(order.id);
        tvId.setTextColor(getResources().getColor(R.color.pg_text, null));
        tvId.setTextSize(13);
        tvId.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams idLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvId.setLayoutParams(idLp);

        TextView tvStatus = new TextView(this);
        tvStatus.setText(statusLabel(order.status));
        tvStatus.setTextSize(10);
        tvStatus.setPadding(dp(10), dp(4), dp(10), dp(4));
        tvStatus.setTypeface(null, Typeface.BOLD);
        applyStatusBadge(tvStatus, order.status);

        row1.addView(tvId);
        row1.addView(tvStatus);

        // Row 2: date + pharmacy
        TextView tvDate = new TextView(this);
        tvDate.setText(order.date + " · " + order.pharmacy + " · " + order.amount);
        tvDate.setTextColor(getResources().getColor(R.color.pg_sub, null));
        tvDate.setTextSize(11);
        LinearLayout.LayoutParams dateLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dateLp.topMargin = dp(4);
        dateLp.bottomMargin = dp(8);
        tvDate.setLayoutParams(dateLp);

        // Row 3: action buttons
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        switch (order.status) {
            case "active":
                addActionText(actions, "Track order →", getResources().getColor(R.color.pg_primary, null), v ->
                        startActivity(new Intent(this, OrderTrackingActivity.class)));
                addActionText(actions, "Cancel", 0xFFEF4444, v ->
                        showCancelDialog(order.id));
                break;
            case "delivered":
                addActionText(actions, "Reorder", getResources().getColor(R.color.pg_primary, null), v -> {
                    Toast.makeText(this, "Adding to cart...", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, CartActivity.class));
                });
                addActionText(actions, "🚩 Report an issue", 0xFFEF4444, v ->
                        startActivity(new Intent(this, ReportIssueActivity.class)));
                break;
            case "cancelled":
                addActionText(actions, "Reorder", getResources().getColor(R.color.pg_primary, null), v ->
                        startActivity(new Intent(this, CartActivity.class)));
                break;
        }

        inner.addView(row1);
        inner.addView(tvDate);
        inner.addView(actions);
        card.addView(inner);
        parent.addView(card);
    }

    private void addActionText(LinearLayout parent, String label, int color, View.OnClickListener listener) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(color);
        tv.setTextSize(12);
        tv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(16);
        tv.setLayoutParams(lp);
        tv.setClickable(true);
        tv.setFocusable(true);
        tv.setOnClickListener(listener);
        parent.addView(tv);
    }

    private void showCancelDialog(String orderId) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.activity_cancel_order_dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialogView.findViewById(R.id.btnKeepOrder).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnConfirmCancel).setOnClickListener(v -> {
            dialog.dismiss();
            // TODO: update Firebase order status to "cancelled"
            Toast.makeText(this, "Order " + orderId + " cancelled.", Toast.LENGTH_SHORT).show();
            // Update local list
            for (Order o : allOrders) {
                if (o.id.equals(orderId)) { o.status = "cancelled"; break; }
            }
            renderOrders();
        });

        dialog.show();
    }

    private String statusLabel(String status) {
        switch (status) {
            case "active":    return "Out for delivery";
            case "delivered": return "Delivered";
            case "cancelled": return "Cancelled";
            default:          return status;
        }
    }

    private void applyStatusBadge(TextView tv, String status) {
        switch (status) {
            case "active":
                tv.setTextColor(0xFF1E40AF);
                tv.setBackgroundResource(R.drawable.bg_tag_blue);
                break;
            case "delivered":
                tv.setTextColor(0xFF065F46);
                tv.setBackgroundResource(R.drawable.bg_tag_green);
                break;
            case "cancelled":
                tv.setTextColor(0xFF991B1B);
                tv.setBackgroundResource(R.drawable.bg_tag_red);
                break;
        }
    }

    private android.widget.ScrollView findScrollView() {
        // Find ScrollView in activity_order_history.xml
        android.view.ViewGroup root = (android.view.ViewGroup) getWindow().getDecorView().getRootView();
        return findScrollViewIn(root);
    }

    private android.widget.ScrollView findScrollViewIn(android.view.ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            if (child instanceof android.widget.ScrollView) return (android.widget.ScrollView) child;
            if (child instanceof android.view.ViewGroup) {
                android.widget.ScrollView found = findScrollViewIn((android.view.ViewGroup) child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class)); finish();
        });
        findViewById(R.id.navSearch).setOnClickListener(v -> {
            Intent i = new Intent(this, MedicineListActivity.class);
            i.putExtra(MedicineListActivity.EXTRA_MODE, "search");
            i.putExtra(MedicineListActivity.EXTRA_TITLE, "Search medicines");
            startActivity(i);
        });
        findViewById(R.id.navCart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class)); finish();
        });
        findViewById(R.id.navOrders).setOnClickListener(v -> { /* already here */ });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class)); finish();
        });
    }
}
