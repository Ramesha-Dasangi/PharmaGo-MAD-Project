package com.nibm.pharmagomadproject.pharmacyowner.reports;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
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

    // Max bar height in dp
    private static final int MAX_BAR_DP = 120;

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
        Context ctx = holder.itemView.getContext();

        holder.txtDay.setText(model.getDay());

        // model.getValue() is 0-100 percentage of max bar height
        // Convert MAX_BAR_DP to pixels for use in LayoutParams
        int maxBarPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, MAX_BAR_DP, ctx.getResources().getDisplayMetrics());

        int barHeightPx = (int) ((model.getValue() / 100.0) * maxBarPx);
        // Minimum visible height of 6dp when value > 0
        if (model.getValue() > 0 && barHeightPx < dpToPx(ctx, 6)) {
            barHeightPx = dpToPx(ctx, 6);
        }

        ViewGroup.LayoutParams params = holder.viewBar.getLayoutParams();
        params.height = barHeightPx;
        holder.viewBar.setLayoutParams(params);

        // Highlight the tallest bar (value == 100) in dark green, others in light green
        if (model.getValue() >= 95) {
            holder.viewBar.setBackgroundColor(Color.parseColor("#2E7D32")); // dark green
            holder.txtDay.setTextColor(Color.parseColor("#2E7D32"));
        } else if (model.getValue() > 0) {
            holder.viewBar.setBackgroundColor(Color.parseColor("#66BB6A")); // medium green
            holder.txtDay.setTextColor(Color.GRAY);
        } else {
            holder.viewBar.setBackgroundColor(Color.parseColor("#E8F5E9")); // very light green (empty)
            holder.txtDay.setTextColor(Color.LTGRAY);
        }
    }

    private int dpToPx(Context ctx, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
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
            txtDay  = itemView.findViewById(R.id.txtDay);
            bar     = itemView.findViewById(R.id.bar);
        }
    }
}