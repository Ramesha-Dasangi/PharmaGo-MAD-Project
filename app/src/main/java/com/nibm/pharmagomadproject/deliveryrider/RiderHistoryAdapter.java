package com.nibm.pharmagomadproject.deliveryrider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.pharmagomadproject.R;

import java.util.List;
import java.util.Map;

public class RiderHistoryAdapter extends RecyclerView.Adapter<RiderHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, String>> orders;

    public RiderHistoryAdapter(Context context, List<Map<String, String>> orders) {
        this.context = context;
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rider_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> order = orders.get(position);

        String orderId = order.get("orderId");
        String status = order.get("status");
        String customerName = order.get("customerName");

        if (orderId != null) {
            holder.tvOrderId.setText("#" + orderId.substring(0, Math.min(8, orderId.length())).toUpperCase());
        }

        if (customerName != null) {
            holder.tvCustomerName.setText(customerName);
        } else {
            holder.tvCustomerName.setText("Customer");
        }

        if (status != null) {
            switch (status.toLowerCase()) {
                case "delivered":
                    holder.tvBadge.setText("Delivered");
                    holder.tvBadge.setTextColor(context.getColor(R.color.green_accept));
                    holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_delivered);
                    break;
                case "cancelled":
                    holder.tvBadge.setText("Cancelled");
                    holder.tvBadge.setTextColor(context.getColor(android.R.color.holo_red_light));
                    holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_cancelled);
                    break;
                case "assigned":
                    holder.tvBadge.setText("Assigned");
                    holder.tvBadge.setTextColor(context.getColor(R.color.primary_orange));
                    holder.tvBadge.setBackgroundResource(R.drawable.bg_status_new);
                    break;
                case "out_for_delivery":
                    holder.tvBadge.setText("In Transit");
                    holder.tvBadge.setTextColor(context.getColor(R.color.primary_orange));
                    holder.tvBadge.setBackgroundResource(R.drawable.bg_status_new);
                    break;
                default:
                    String displayStatus = status.substring(0, 1).toUpperCase() + status.substring(1).replace("_", " ");
                    holder.tvBadge.setText(displayStatus);
                    holder.tvBadge.setTextColor(context.getColor(R.color.text_secondary));
                    holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_delivered);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvBadge, tvCustomerName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
        }
    }
}
