package com.nibm.pharmagomadproject.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.models.Complaint;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {

    private final List<Complaint> complaints;

    public ComplaintAdapter(List<Complaint> complaints) {
        this.complaints = complaints;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Complaint c = complaints.get(position);
        h.tvTarget.setText(c.getTargetName());
        h.tvType.setText(c.getTargetType().equals("pharmacy") ? "Pharmacy" : "Rider");
        h.tvReason.setText(c.getReason());
        h.tvDesc.setText(c.getDescription());
        h.tvStatus.setText(c.getStatus() != null
                ? c.getStatus().substring(0,1).toUpperCase() + c.getStatus().substring(1)
                : "Pending");
    }

    @Override
    public int getItemCount() { return complaints.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTarget, tvType, tvReason, tvDesc, tvStatus;

        ViewHolder(View v) {
            super(v);
            tvTarget = v.findViewById(R.id.tvTargetName);
            tvType   = v.findViewById(R.id.tvComplaintType);
            tvReason = v.findViewById(R.id.tvReason);
            tvDesc   = v.findViewById(R.id.tvDescription);
            tvStatus = v.findViewById(R.id.tvComplaintStatus);
        }
    }
}
