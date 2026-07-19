package com.nibm.pharmagomadproject.Admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PendingPharmacyAdapter extends RecyclerView.Adapter<PendingPharmacyAdapter.ViewHolder> {

    public interface OnReviewClickListener {
        void onReviewClick(PendingPharmacyModel pharmacy);
    }

    private final List<PendingPharmacyModel> items = new ArrayList<>();
    private final OnReviewClickListener listener;

    public PendingPharmacyAdapter(OnReviewClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<PendingPharmacyModel> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_pharmacy, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PendingPharmacyModel pharmacy = items.get(position);

        holder.tvName.setText(pharmacy.getName() != null ? pharmacy.getName() : "Unnamed pharmacy");

        String ownerLine = "Owner: " + (pharmacy.getOwnerName() != null ? pharmacy.getOwnerName() : "-");
        holder.tvTime.setText(ownerLine + "  •  " + timeAgo(pharmacy.getCreatedAt()));

        String status = pharmacy.getStatus() != null ? pharmacy.getStatus() : "pending";
        holder.tvStatus.setText(capitalize(status));

        holder.btnReview.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReviewClick(pharmacy);
            }
        });

        if (pharmacy.getLicenseImageUrl() != null && !pharmacy.getLicenseImageUrl().isEmpty()) {
            holder.btnViewDoc.setVisibility(View.VISIBLE);
            holder.btnViewDoc.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse(pharmacy.getLicenseImageUrl()));
                v.getContext().startActivity(intent);
            });
        } else {
            holder.btnViewDoc.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String timeAgo(Timestamp timestamp) {
        if (timestamp == null) return "";
        Date date = timestamp.toDate();
        long diffMs = System.currentTimeMillis() - date.getTime();
        long minutes = diffMs / (60 * 1000);
        long hours = minutes / 60;
        long days = hours / 24;

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        return days + (days == 1 ? " day ago" : " days ago");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvTime;
        TextView tvStatus;
        MaterialButton btnReview;
        MaterialButton btnViewDoc;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnReview = itemView.findViewById(R.id.btnReview);
            btnViewDoc = itemView.findViewById(R.id.btnViewDoc);
        }
    }
}
