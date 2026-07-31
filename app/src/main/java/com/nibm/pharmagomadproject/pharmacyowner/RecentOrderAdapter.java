package com.nibm.pharmagomadproject.pharmacyowner;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;

public class RecentOrderAdapter extends RecyclerView.Adapter<RecentOrderAdapter.ViewHolder> {

    private Context context;
    private ArrayList<OrderModel> orderList;

    public RecentOrderAdapter(Context context, ArrayList<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recent_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = orderList.get(position);

        holder.txtOrderId.setText(order.getOrderId());
        holder.txtCustomer.setText(order.getCustomerName());
        holder.txtItems.setText(order.getItemCount());
        holder.txtTime.setText(order.getTime());
        holder.txtAmount.setText(order.getAmount());

        holder.txtStatusBadge.setText(order.getStatus());

        // Status badge color coding
        if (order.getStatus().equalsIgnoreCase("New")) {
            holder.txtStatusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        } else if (order.getStatus().equalsIgnoreCase("Processing")) {
            holder.txtStatusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#FF9800")));
        } else {
            holder.txtStatusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#2196F3")));
        }

        // View order details on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailsActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            intent.putExtra("customerName", order.getCustomerName());
            intent.putExtra("customerId", order.getCustomerId());
            intent.putExtra("items", order.getItemCount());
            intent.putExtra("time", order.getTime());
            intent.putExtra("amount", order.getAmount());
            intent.putExtra("status", order.getStatus());
            intent.putExtra("type", order.getType());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtCustomer, txtItems, txtTime, txtAmount, txtStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderId     = itemView.findViewById(R.id.txtOrderId);
            txtCustomer    = itemView.findViewById(R.id.txtCustomer);
            txtItems       = itemView.findViewById(R.id.txtItems);
            txtTime        = itemView.findViewById(R.id.txtTime);
            txtAmount      = itemView.findViewById(R.id.txtAmount);
            txtStatusBadge = itemView.findViewById(R.id.txtStatusBadge);
        }
    }
}
