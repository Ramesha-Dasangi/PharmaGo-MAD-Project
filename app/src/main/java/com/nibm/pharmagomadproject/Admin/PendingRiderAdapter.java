package com.nibm.pharmagomadproject.Admin;

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

public class PendingRiderAdapter extends RecyclerView.Adapter<PendingRiderAdapter.RiderViewHolder> {

    private List<PendingRiderModel> riders = new ArrayList<>();
    private final OnRiderClickListener listener;

    public interface OnRiderClickListener {
        void onReviewClick(PendingRiderModel rider);
    }

    public PendingRiderAdapter(OnRiderClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<PendingRiderModel> newRiders) {
        this.riders = newRiders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RiderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_rider, parent, false);
        return new RiderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiderViewHolder holder, int position) {
        PendingRiderModel rider = riders.get(position);
        
        String name = rider.getName();
        holder.tvRiderName.setText(name == null || name.trim().isEmpty() ? "Unknown Rider" : name);
        
        String timeAgoStr = timeAgo(rider.getCreatedAt());
        if(rider.getEmail() != null && !rider.getEmail().isEmpty()) {
            holder.tvRiderTime.setText(rider.getEmail() + "  •  " + timeAgoStr);
        } else {
            holder.tvRiderTime.setText("No email provided  •  " + timeAgoStr);
        }

        holder.btnReviewRider.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReviewClick(rider);
            }
        });

        if (rider.getLicenseUrl() != null && !rider.getLicenseUrl().isEmpty()) {
            holder.btnViewDocRider.setVisibility(View.VISIBLE);
            holder.btnViewDocRider.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse(rider.getLicenseUrl()));
                v.getContext().startActivity(intent);
            });
        } else {
            holder.btnViewDocRider.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return riders.size();
    }

    private static String timeAgo(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) return "";
        java.util.Date date = timestamp.toDate();
        long diffMs = System.currentTimeMillis() - date.getTime();
        long minutes = diffMs / (60 * 1000);
        long hours = minutes / 60;
        long days = hours / 24;

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        return days + (days == 1 ? " day ago" : " days ago");
    }

    static class RiderViewHolder extends RecyclerView.ViewHolder {
        TextView tvRiderName, tvRiderTime;
        MaterialButton btnReviewRider, btnViewDocRider;

        public RiderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRiderName = itemView.findViewById(R.id.tvRiderName);
            tvRiderTime = itemView.findViewById(R.id.tvRiderTime);
            btnReviewRider = itemView.findViewById(R.id.btnReviewRider);
            btnViewDocRider = itemView.findViewById(R.id.btnViewDocRider);
        }
    }
}
