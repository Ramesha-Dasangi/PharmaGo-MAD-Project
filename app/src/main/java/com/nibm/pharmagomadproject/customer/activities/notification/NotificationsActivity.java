package com.nibm.pharmagomadproject.customer.activities.notification;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.adapter.NotificationAdapter;
import com.nibm.pharmagomadproject.customer.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView          rvNotifications;
    private NotificationAdapter   adapter;
    private final List<Notification> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications_customer);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        rvNotifications = findViewById(R.id.rvNotifications);
        if (rvNotifications != null) {
            adapter = new NotificationAdapter(notifications, notif -> {
                // Mark as read on click
                if (!notif.isRead()) {
                    notif.setRead(true);
                    if (notif.getNotificationId() != null) {
                        FirebaseFirestore.getInstance()
                                .collection("notifications")
                                .document(notif.getNotificationId())
                                .update("isRead", true);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
            rvNotifications.setLayoutManager(new LinearLayoutManager(this));
            rvNotifications.setAdapter(adapter);
        }

        loadNotifications();
    }

    private void loadNotifications() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    notifications.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Notification n = new Notification();
                        n.setNotificationId(doc.getId());
                        // set fields manually
                        String title   = doc.getString("title");
                        String message = doc.getString("message");
                        Boolean isRead = doc.getBoolean("isRead");

                        // Build a notification object via reflection-safe setter
                        // (Notification has a multi-arg ctor)
                        Notification notif = new Notification(
                                uid,
                                title   != null ? title   : "",
                                message != null ? message : "",
                                doc.getString("type") != null ? doc.getString("type") : "",
                                doc.getString("referenceId") != null ? doc.getString("referenceId") : ""
                        );
                        notif.setNotificationId(doc.getId());
                        if (Boolean.TRUE.equals(isRead)) notif.setRead(true);
                        notifications.add(notif);
                    }
                    if (adapter != null) adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show());
    }
}
