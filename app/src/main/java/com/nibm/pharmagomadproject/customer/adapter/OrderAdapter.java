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

        h.tvOrderId.setText("#" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())).toUpperCase());
        h.tvPharmacy.setText(order.getPharmacyName());
        h.tvTotal.setText("Rs. " + String.format("%.0f", order.getTotal()));
        h.tvStatus.setText(order.getStatusDisplay());

        String status = order.getStatus();

        // Show/hide action buttons based on status
        boolean isActive = "pending".equals(status) || "processing".equals(status)
                || "picked_up".equals(status) || "out_for_delivery".equals(status);
        boolean isPendingPay = "approved_pending_payment".equals(status);
        boolean isAwaitingApproval = "awaiting_approval".equals(status);
        boolean isDelivered = "delivered".equals(status);
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

        if (h.btnTrack != null)
            h.btnTrack.setOnClickListener(v -> listener.onTrackOrder(order));
        if (h.btnCancel != null)
            h.btnCancel.setOnClickListener(v -> listener.onCancelOrder(order));
        if (h.btnReorder != null)
            h.btnReorder.setOnClickListener(v -> listener.onReorder(order));
        if (h.btnReport != null)
            h.btnReport.setOnClickListener(v -> listener.onReportIssue(order));
    }

    @Override
    public int getItemCount() { return orderList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvPharmacy, tvTotal, tvStatus;
        TextView btnTrack, btnCancel, btnReorder, btnReport;

        ViewHolder(View v) {
            super(v);
            tvOrderId  = v.findViewById(R.id.tvOrderId);
            tvPharmacy = v.findViewById(R.id.tvPharmacy);
            tvTotal    = v.findViewById(R.id.tvTotal);
            tvStatus   = v.findViewById(R.id.tvStatus);
            btnTrack   = v.findViewById(R.id.btnTrackOrder);
            btnCancel  = v.findViewById(R.id.btnCancelOrder);
            btnReorder = v.findViewById(R.id.btnReorder1);
            btnReport  = v.findViewById(R.id.btnReport1);
        }
    }
}
