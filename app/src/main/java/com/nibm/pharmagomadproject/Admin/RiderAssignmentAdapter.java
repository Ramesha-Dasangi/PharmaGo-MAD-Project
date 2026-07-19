package com.nibm.pharmagomadproject.Admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.List;

public class RiderAssignmentAdapter extends RecyclerView.Adapter<RiderAssignmentAdapter.RiderViewHolder> {

    private final List<UserModel> riders = new ArrayList<>();
    private int selectedPosition = -1;
    private OnRiderSelectedListener listener;

    public interface OnRiderSelectedListener {
        void onRiderSelected(UserModel rider);
    }

    public void setListener(OnRiderSelectedListener listener) {
        this.listener = listener;
    }

    public void setRiders(List<UserModel> newRiders) {
        riders.clear();
        if (newRiders != null) riders.addAll(newRiders);
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RiderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assign_rider, parent, false);
        return new RiderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RiderViewHolder holder, int position) {
        UserModel rider = riders.get(position);
        Context ctx = holder.itemView.getContext();
        
        holder.tvName.setText(rider.getName() != null ? rider.getName() : "Unknown Rider");
        
        String phone = rider.getPhone();
        holder.tvSub.setText(phone != null ? phone : "No phone number");

        boolean isSelected = (position == selectedPosition);

        if (isSelected) {
            holder.card.setStrokeColor(ContextCompat.getColor(ctx, R.color.colorAccent));
            holder.ivIcon.setBackgroundResource(R.drawable.icon_bg_green);
            holder.ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.colorAccent)));
            holder.ivCheck.setImageResource(android.R.drawable.checkbox_on_background);
            holder.ivCheck.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.colorAccent)));
        } else {
            holder.card.setStrokeColor(ContextCompat.getColor(ctx, R.color.colorStroke));
            holder.ivIcon.setBackgroundResource(R.drawable.icon_bg_blue);
            holder.ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.colorTextSecondary)));
            holder.ivCheck.setImageResource(android.R.drawable.checkbox_off_background);
            holder.ivCheck.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.colorStroke)));
        }

        holder.itemView.setOnClickListener(v -> {
            int oldSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldSelected);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onRiderSelected(rider);
            }
        });
    }

    @Override
    public int getItemCount() {
        return riders.size();
    }

    static class RiderViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView ivIcon, ivCheck;
        TextView tvName, tvSub;

        RiderViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardRider);
            ivIcon = itemView.findViewById(R.id.ivIconRider);
            ivCheck = itemView.findViewById(R.id.ivCheckRider);
            tvName = itemView.findViewById(R.id.tvRiderName);
            tvSub = itemView.findViewById(R.id.tvRiderSub);
        }
    }
}
