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

import com.nibm.pharmagomadproject.pharmacyowner.OrderModel;
import com.nibm.pharmagomadproject.pharmacyowner.VerifyPrescriptionActivity;

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

        // Status Button Colors

        if (order.getStatus().equalsIgnoreCase("New")) {

            holder.btnStatus.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green

        } else if (order.getStatus().equalsIgnoreCase("Processing")) {

            holder.btnStatus.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#FF9800"))); // Orange

        } else {

            holder.btnStatus.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#2196F3"))); // Blue

        }

        holder.btnStatus.setTextColor(Color.WHITE);

        // Medicine Type Button

        if (order.getType().equalsIgnoreCase("RX Required")
                || order.getType().equalsIgnoreCase("Rx Required")) {

            holder.btnType.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#E53935"))); // Red

            holder.btnType.setTextColor(Color.WHITE);

            holder.btnType.setOnClickListener(v -> {

                Intent intent = new Intent(
                        context,
                        VerifyPrescriptionActivity.class);

                context.startActivity(intent);

            });

        } else {

            holder.btnType.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green

            holder.btnType.setTextColor(Color.WHITE);

            holder.btnType.setOnClickListener(v -> {

                // OTC Order
                // No prescription verification required

            });

        }

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtOrderId;
        TextView txtCustomer;
        TextView txtItems;
        TextView txtTime;
        TextView txtAmount;

        Button btnStatus;
        Button btnType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtCustomer = itemView.findViewById(R.id.txtCustomer);
            txtItems = itemView.findViewById(R.id.txtItems);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtAmount = itemView.findViewById(R.id.txtAmount);

            btnStatus = itemView.findViewById(R.id.btnStatus);
            btnType = itemView.findViewById(R.id.btnType);

        }
    }
}