package com.nibm.pharmagomadproject.pharmacyowner;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context context;
    private ArrayList<OrderModel> orderList;

    public OrderAdapter(Context context, ArrayList<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.order_item, parent, false);

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

        holder.btnStatus.setText(order.getStatus());
        holder.btnType.setText(order.getType());

        // STATUS COLOR
        if (order.getStatus().equalsIgnoreCase("New")) {

            holder.btnStatus.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#4CAF50")));

        } else if (order.getStatus().equalsIgnoreCase("Processing")) {

            holder.btnStatus.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#FF9800")));

        } else {

            holder.btnStatus.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#2196F3")));
        }

        holder.btnStatus.setTextColor(Color.WHITE);

        // TYPE BUTTON
        if (order.getType().equalsIgnoreCase("RX Required")) {

            holder.btnType.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#E53935")));

            holder.btnType.setTextColor(Color.WHITE);

            holder.btnType.setOnClickListener(v -> {
                Intent intent = new Intent(context, VerifyPrescriptionActivity.class);
                intent.putExtra("orderId", order.getOrderId());
                intent.putExtra("customerName", order.getCustomerName());
                intent.putExtra("customerId", order.getCustomerId());
                context.startActivity(intent);
            });

        } else {

            holder.btnType.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#4CAF50")));

            holder.btnType.setTextColor(Color.WHITE);
        }

        // ✅ VIEW BUTTON CLICK
        holder.btnView.setOnClickListener(v -> {

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

        TextView txtOrderId, txtCustomer, txtItems, txtTime, txtAmount;
        Button btnStatus, btnType, btnView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtCustomer = itemView.findViewById(R.id.txtCustomer);
            txtItems = itemView.findViewById(R.id.txtItems);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtAmount = itemView.findViewById(R.id.txtAmount);

            btnStatus = itemView.findViewById(R.id.btnStatus);
            btnType = itemView.findViewById(R.id.btnType);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}