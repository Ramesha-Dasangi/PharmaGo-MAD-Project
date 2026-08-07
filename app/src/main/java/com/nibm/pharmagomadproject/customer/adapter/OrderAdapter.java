package com.nibm.pharmagomadproject.customer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.models.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OrderListener {
        void onTrackOrder(Order order);
        void onReorder(Order order);
        void onReportIssue(Order order);
        void onCancelOrder(Order order);
        void onRateOrder(Order order);
    }

    private final Context       context;
    private final List<Order>   orderList;
    private final OrderListener listener;

    public OrderAdapter(Context context, List<Order> orderList, OrderListener listener) {
        this.context   = context;
        this.orderList = orderList;
        this.listener  = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Order order = orderList.get(position);

        // Show FULL Order ID (no truncation)
        h.tvOrderId.setText("#" + (order.getOrderId() != null ? order.getOrderId() : "").toUpperCase());
        h.tvPharmacy.setText(order.getPharmacyNamesDisplay());
        h.tvTotal.setText("Rs. " + String.format("%.0f", order.getTotal()));
        h.tvStatus.setText(order.getStatusDisplay());

        // Format Date & Time
        if (h.tvOrderDate != null) {
            if (order.getCreatedAt() != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy · hh:mm a", java.util.Locale.getDefault());
                h.tvOrderDate.setText(sdf.format(order.getCreatedAt().toDate()));
                h.tvOrderDate.setVisibility(View.VISIBLE);
            } else {
                h.tvOrderDate.setVisibility(View.GONE);
            }
        }

        String status = order.getStatus();

        // Show/hide action buttons based on status
        boolean isActive = "pending".equals(status) || "processing".equals(status)
                || "assigned".equals(status) || "picked_up".equals(status) || "out_for_delivery".equals(status);
        boolean isPendingPay = "approved_pending_payment".equals(status);
        boolean isAwaitingApproval = "awaiting_approval".equals(status);
        boolean isDelivered = "delivered".equals(status) || "completed".equals(status);
        boolean isCancelled = "cancelled".equals(status);

        if (h.btnTrack != null) {
            if (isPendingPay) {
                h.btnTrack.setVisibility(View.VISIBLE);
                h.btnTrack.setText("Pay");
            } else if (isActive) {
                h.btnTrack.setVisibility(View.VISIBLE);
                h.btnTrack.setText("Track");
            } else {
                h.btnTrack.setVisibility(View.GONE);
            }
        }

        if (h.btnCancel != null) {
            h.btnCancel.setVisibility((isActive || isPendingPay || isAwaitingApproval) ? View.VISIBLE : View.GONE);
        }

        if (h.btnReorder != null)
            h.btnReorder.setVisibility(isDelivered ? View.VISIBLE : View.GONE);
        if (h.btnReport != null)
            h.btnReport.setVisibility(isDelivered ? View.VISIBLE : View.GONE);
        if (h.btnRate != null)
            h.btnRate.setVisibility(isDelivered ? View.VISIBLE : View.GONE);


        // Apply status tag colour (background + text)
        setStatusTagColor(context, h.tvStatus, status);

        h.itemView.setOnClickListener(v -> listener.onTrackOrder(order));
        if (h.btnTrack != null)
            h.btnTrack.setOnClickListener(v -> listener.onTrackOrder(order));
        if (h.btnCancel != null)
            h.btnCancel.setOnClickListener(v -> listener.onCancelOrder(order));
        if (h.btnReorder != null)
            h.btnReorder.setOnClickListener(v -> listener.onReorder(order));
        if (h.btnReport != null)
            h.btnReport.setOnClickListener(v -> listener.onReportIssue(order));
        if (h.btnRate != null)
            h.btnRate.setOnClickListener(v -> listener.onRateOrder(order));
    }

    /**
     * Applies the correct background drawable + text colour to the status badge based on order
     * status, honouring the values-night colour aliases so it looks correct in both themes.
     */
    private static void setStatusTagColor(android.content.Context ctx, TextView tv, String status) {
        if (tv == null || status == null) return;
        int bgRes;
        int fgRes;
        switch (status.toLowerCase()) {
            case "delivered":
            case "completed":
                bgRes = com.nibm.pharmagomadproject.R.drawable.bg_tag_green;
                fgRes = com.nibm.pharmagomadproject.R.color.tag_green_text;
                break;
            case "pending":
            case "processing":
            case "assigned":
            case "picked_up":
            case "out_for_delivery":
            case "partially_approved":
                bgRes = com.nibm.pharmagomadproject.R.drawable.bg_tag_blue;
                fgRes = com.nibm.pharmagomadproject.R.color.tag_blue_text;
                break;
            case "awaiting_approval":
            case "approved_pending_payment":
                bgRes = com.nibm.pharmagomadproject.R.drawable.bg_tag_amber;
                fgRes = com.nibm.pharmagomadproject.R.color.tag_amber_text;
                break;
            case "cancelled":
            case "rejected":
            case "partially_rejected":
                bgRes = com.nibm.pharmagomadproject.R.drawable.bg_tag_red;
                fgRes = com.nibm.pharmagomadproject.R.color.tag_red_text;
                break;
            default:
                bgRes = com.nibm.pharmagomadproject.R.drawable.bg_tag_blue;
                fgRes = com.nibm.pharmagomadproject.R.color.tag_blue_text;
        }
        tv.setBackgroundResource(bgRes);
        tv.setTextColor(ctx.getResources().getColor(fgRes, ctx.getTheme()));
    }

    @Override
    public int getItemCount() { return orderList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvPharmacy, tvTotal, tvStatus, tvOrderDate;
        TextView btnTrack, btnCancel, btnReorder, btnReport, btnRate;

        ViewHolder(View v) {
            super(v);
            tvOrderId   = v.findViewById(R.id.tvOrderId);
            tvPharmacy  = v.findViewById(R.id.tvPharmacy);
            tvTotal     = v.findViewById(R.id.tvTotal);
            tvStatus    = v.findViewById(R.id.tvStatus);
            tvOrderDate = v.findViewById(R.id.tvOrderDate);
            btnTrack    = v.findViewById(R.id.btnTrackOrder);
            btnCancel   = v.findViewById(R.id.btnCancelOrder);
            btnReorder  = v.findViewById(R.id.btnReorder1);
            btnReport   = v.findViewById(R.id.btnReport1);
            btnRate     = v.findViewById(R.id.btnRate);
        }
    }

}