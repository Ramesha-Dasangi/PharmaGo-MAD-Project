package com.nibm.pharmagomadproject.Admin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<OrderModel> orders = new ArrayList<>();

    public void setOrders(List<OrderModel> newOrders) {
        orders.clear();
        if (newOrders != null) orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orders.get(position);

        // Order ID
        String displayId = order.getOrderId() != null ? "Order #" + order.getOrderId() : "Order #" + order.getId();
        holder.tvOrderId.setText(displayId);

        // Status
        String status = order.getStatus() != null ? capitalize(order.getStatus()) : "Pending";
        holder.tvOrderStatus.setText(status);

        // Items
        int itemCount = order.getItemCount();
        holder.tvOrderItems.setText(itemCount + (itemCount == 1 ? " item" : " items"));

        // Address
        String addr = order.getDeliveryAddress();
        holder.tvOrderAddress.setText(addr != null && !addr.isEmpty() ? "📍 " + addr : "No address set");

        // Total
        holder.tvOrderTotal.setText(String.format("Rs. %.2f", order.getTotal()));

        // Time ago
        holder.tvOrderTime.setText(timeAgo(order.getCreatedAt()));

        // Assign button and Rider Name
        boolean isAssigned = order.getRiderId() != null && !order.getRiderId().isEmpty();
        
        if (isAssigned && order.getRiderName() != null && !order.getRiderName().isEmpty()) {
            holder.tvRiderAssigned.setVisibility(View.VISIBLE);
            holder.tvRiderAssigned.setText("Assigned to: " + order.getRiderName());
        } else {
            holder.tvRiderAssigned.setVisibility(View.GONE);
        }

        holder.btnAssignRider.setText(isAssigned ? "Reassign Rider" : "Assign Rider");
        
        holder.btnAssignRider.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AssignRiderActivity.class);
            intent.putExtra("ORDER_ID", order.getId());
            intent.putExtra("ORDER_DISPLAY_ID", displayId);
            intent.putExtra("ORDER_DETAILS", itemCount + " item(s)  •  " + (addr != null ? addr : ""));
            if (isAssigned) {
                intent.putExtra("OLD_RIDER_ID", order.getRiderId());
            }
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return orders.size(); }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String timeAgo(long millis) {
        if (millis == 0) return "";
        long diffMs = System.currentTimeMillis() - millis;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs);
        long hours = TimeUnit.MILLISECONDS.toHours(diffMs);
        long days = TimeUnit.MILLISECONDS.toDays(diffMs);
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + (minutes == 1 ? " min ago" : " mins ago");
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        return days + (days == 1 ? " day ago" : " days ago");
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvOrderItems, tvOrderAddress, tvOrderTotal, tvOrderTime, tvRiderAssigned;
        MaterialButton btnAssignRider;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvOrderAddress = itemView.findViewById(R.id.tvOrderAddress);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvRiderAssigned = itemView.findViewById(R.id.tvRiderAssigned);
            btnAssignRider = itemView.findViewById(R.id.btnAssignRider);
        }
    }
}
