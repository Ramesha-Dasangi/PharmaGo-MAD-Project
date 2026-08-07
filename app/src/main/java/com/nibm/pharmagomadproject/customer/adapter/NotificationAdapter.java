package com.nibm.pharmagomadproject.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface NotifListener {
        void onClick(Notification notif);
        void onDelete(Notification notif);
    }

    private final List<Notification> notifications;
    private final NotifListener      listener;

    public NotificationAdapter(List<Notification> notifications, NotifListener listener) {
        this.notifications = notifications;
        this.listener      = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Notification n = notifications.get(position);
        h.tvTitle.setText(n.getTitle());
        h.tvMessage.setText(n.getMessage());

        if (h.tvTime != null) {
            String timeStr = n.getFormattedTime();
            if (timeStr != null && !timeStr.isEmpty()) {
                h.tvTime.setText(timeStr);
                h.tvTime.setVisibility(View.VISIBLE);
            } else {
                h.tvTime.setVisibility(View.GONE);
            }
        }

        // Unread — bold
        h.tvTitle.setTypeface(null, n.isRead()
                ? android.graphics.Typeface.NORMAL
                : android.graphics.Typeface.BOLD);

        h.itemView.setOnClickListener(v -> listener.onClick(n));
        h.itemView.setOnLongClickListener(v -> {
            listener.onDelete(n);
            return true;
        });
    }


    @Override
    public int getItemCount() { return notifications.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;

        ViewHolder(View v) {
            super(v);
            tvTitle   = v.findViewById(R.id.tvNotifTitle);
            tvMessage = v.findViewById(R.id.tvNotifMessage);
            tvTime    = v.findViewById(R.id.tvNotifTime);
        }
    }
}
