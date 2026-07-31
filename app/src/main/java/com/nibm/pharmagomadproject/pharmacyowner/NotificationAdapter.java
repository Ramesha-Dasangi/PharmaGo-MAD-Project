package com.nibm.pharmagomadproject.pharmacyowner;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private ArrayList<NotificationModel> list;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationModel notification);
    }

    public NotificationAdapter(Context context, ArrayList<NotificationModel> list) {
        this.context = context;
        this.list = list;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel model = list.get(position);

        holder.txtTitle.setText(model.getTitle());
        holder.txtDescription.setText(model.getDescription());
        holder.txtTime.setText(model.getTime());

        // Visual distinction for Read vs Unread
        if (model.isRead()) {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.viewColor.setBackgroundColor(Color.parseColor("#CCCCCC")); // Muted Gray
        } else {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F0FDF4")); // Soft Light Green
            holder.viewColor.setBackgroundColor(ContextCompat.getColor(context, R.color.green)); // Active Green
        }

        // Set Icon based on type
        if ("stock".equalsIgnoreCase(model.getType())) {
            holder.imgIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            holder.imgIcon.setColorFilter(Color.parseColor("#DC2626")); // Crimson Red for low stock
            if (!model.isRead()) {
                holder.viewColor.setBackgroundColor(Color.parseColor("#DC2626"));
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FEF2F2")); // Soft red background
            }
        } else if ("order".equalsIgnoreCase(model.getType())) {
            holder.imgIcon.setImageResource(android.R.drawable.ic_menu_myplaces);
            holder.imgIcon.setColorFilter(ContextCompat.getColor(context, R.color.green));
        } else {
            holder.imgIcon.setImageResource(android.R.drawable.ic_dialog_info);
            holder.imgIcon.setColorFilter(Color.parseColor("#2563EB")); // Blue for general info
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription, txtTime;
        CardView cardView;
        View viewColor;
        ImageView imgIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtTime = itemView.findViewById(R.id.txtTime);
            cardView = itemView.findViewById(R.id.cardNotification);
            viewColor = itemView.findViewById(R.id.viewColor);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}