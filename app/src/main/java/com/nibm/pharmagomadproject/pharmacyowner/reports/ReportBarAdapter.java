package com.nibm.pharmagomadproject.pharmacyowner.reports;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;

public class ReportBarAdapter extends RecyclerView.Adapter<ReportBarAdapter.ViewHolder> {

    private ArrayList<SalesReportModel> reportList;

    public ReportBarAdapter(ArrayList<SalesReportModel> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.report_bar_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        SalesReportModel model = reportList.get(position);

        holder.txtDay.setText(model.getDay());

        // Maximum bar height = 140dp
        int height = (int) (model.getValue() * 2);

        ViewGroup.LayoutParams params = holder.viewBar.getLayoutParams();
        params.height = height;
        holder.viewBar.setLayoutParams(params);

        // Highlight Thursday like the design

        if (model.getDay().equals("Thu")) {

            holder.viewBar.setBackgroundColor(
                    Color.parseColor("#2E7D32"));

            holder.txtDay.setTextColor(
                    Color.parseColor("#2E7D32"));

        } else {

            holder.viewBar.setBackgroundColor(
                    Color.parseColor("#C8E6C9"));

            holder.txtDay.setTextColor(Color.GRAY);

        }

    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View viewBar;
        TextView txtDay;
        LinearLayout bar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            viewBar = itemView.findViewById(R.id.viewBar);
            txtDay = itemView.findViewById(R.id.txtDay);
            bar = itemView.findViewById(R.id.bar);
        }
    }
}