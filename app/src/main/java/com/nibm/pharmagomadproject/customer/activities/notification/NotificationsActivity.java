package com.nibm.pharmagomadproject.customer.activities.notification;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.activities.order.PaymentActivity;
import com.nibm.pharmagomadproject.customer.adapter.NotificationAdapter;
import com.nibm.pharmagomadproject.customer.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView          rvNotifications;
    private NotificationAdapter   adapter;
    private final List<Notification> notifications = new ArrayList<>();
    private ListenerRegistration  listenerRegistration;

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
                
                // If it is prescription approved, click navigates to payment
                if ("prescription_approved".equalsIgnoreCase(notif.getType()) && notif.getReferenceId() != null && !notif.getReferenceId().isEmpty()) {
                    Intent intent = new Intent(this, PaymentActivity.class);
                    intent.putExtra("orderId", notif.getReferenceId());
                    startActivity(intent);
                }
            });
            rvNotifications.setLayoutManager(new LinearLayoutManager(this));
            rvNotifications.setAdapter(adapter);
        }

        subscribeNotifications();
    }

    private void subscribeNotifications() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (uid == null) {
            Toast.makeText(this, "Please log in to view notifications", Toast.LENGTH_SHORT).show();
            return;
        }

        // Real-time listener — ordered by createdAt descending
        // Requires a Firestore composite index on notifications(userId, createdAt DESC)
        listenerRegistration = FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((query, error) -> {
                    if (error != null) {
                        // Likely a missing Firestore composite index — fallback to unordered query
                        loadNotificationsFallback(uid);
                        return;
                    }
                    if (query == null) return;
                    populateNotifications(uid, query);
                });
    }

    // simple query without ordering (no composite index needed)
    private void loadNotificationsFallback(String uid) {
        FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    notifications.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        buildNotification(uid, doc);
                    }
                    // Sort in memory by createdAt descending
                    notifications.sort((a, b) -> {
                        com.google.firebase.Timestamp ta = a.getCreatedAt();
                        com.google.firebase.Timestamp tb = b.getCreatedAt();
                        if (ta == null) return 1;
                        if (tb == null) return -1;
                        // Firebase Timestamp: compare seconds, then nanos
                        int cmp = Long.compare(tb.getSeconds(), ta.getSeconds());
                        return cmp != 0 ? cmp : Integer.compare(tb.getNanoseconds(), ta.getNanoseconds());
                    });
                    if (adapter != null) adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show());
    }

    private void populateNotifications(String uid, com.google.firebase.firestore.QuerySnapshot query) {
        notifications.clear();
        for (QueryDocumentSnapshot doc : query) {
            buildNotification(uid, doc);
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void buildNotification(String uid, QueryDocumentSnapshot doc) {
        String title   = doc.getString("title");
        String message = doc.getString("message");
        Boolean isRead = doc.getBoolean("isRead");

        Notification notif = new Notification(
                uid,
                title   != null ? title   : "",
                message != null ? message : "",
                doc.getString("type")        != null ? doc.getString("type")        : "",
                doc.getString("referenceId") != null ? doc.getString("referenceId") : ""
        );
        notif.setNotificationId(doc.getId());
        if (Boolean.TRUE.equals(isRead)) notif.setRead(true);
        notifications.add(notif);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener to prevent memory leaks
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}
