package com.nibm.pharmagomadproject.Admin;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.nibm.pharmagomadproject.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {

    private final Context context;
    private final List<ComplaintModel> complaintList;
    private final OnComplaintActionListener actionListener;

    public interface OnComplaintActionListener {
        void onResolve(ComplaintModel complaint);
        void onView(ComplaintModel complaint);
    }

    public ComplaintAdapter(Context context, List<ComplaintModel> complaintList, OnComplaintActionListener actionListener) {
        this.context = context;
        this.complaintList = complaintList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        ComplaintModel complaint = complaintList.get(position);

        holder.tvTargetName.setText(complaint.getTargetName() != null ? complaint.getTargetName() : "Unknown");
        holder.tvComplaintType.setText(complaint.getType() != null ? complaint.getType().toUpperCase() : "GENERAL");
        holder.tvReason.setText(complaint.getCategory() != null ? complaint.getCategory() : "No category");
        
        // Hide description in the list item so it's only shown on View
        holder.tvDescription.setVisibility(View.GONE);

        // Status styling
        String status = complaint.getStatus();
        if ("pending".equalsIgnoreCase(status)) {
            holder.tvComplaintStatus.setText("Pending");
            holder.tvComplaintStatus.setBackgroundResource(R.drawable.bg_warning_red); // assuming exists, or set color manually
            holder.tvComplaintStatus.setTextColor(Color.RED);
            holder.btnResolve.setVisibility(View.VISIBLE);
        } else {
            holder.tvComplaintStatus.setText("Resolved");
            holder.tvComplaintStatus.setBackgroundResource(R.drawable.bg_tag_green);
            holder.tvComplaintStatus.setTextColor(Color.parseColor("#388E3C")); // green
            holder.btnResolve.setVisibility(View.GONE);
        }

        // Actions
        holder.btnResolve.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onResolve(complaint);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onView(complaint);
            }
        });
    }

    @Override
    public int getItemCount() {
        return complaintList != null ? complaintList.size() : 0;
    }

    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView tvTargetName, tvComplaintType, tvReason, tvDescription, tvComplaintStatus;
        MaterialButton btnResolve;

        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTargetName = itemView.findViewById(R.id.tvTargetName);
            tvComplaintType = itemView.findViewById(R.id.tvComplaintType);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvComplaintStatus = itemView.findViewById(R.id.tvComplaintStatus);
            btnResolve = itemView.findViewById(R.id.btnResolve);
        }
    }
}
