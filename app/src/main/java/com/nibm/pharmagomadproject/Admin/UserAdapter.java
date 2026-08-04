package com.nibm.pharmagomadproject.Admin;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.nibm.pharmagomadproject.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<UserModel> users = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void setUsers(List<UserModel> newUsers) {
        users.clear();
        if (newUsers != null) users.addAll(newUsers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = users.get(position);
        Context ctx = holder.itemView.getContext();

        holder.tvName.setText(user.getName() != null ? user.getName() : "Unknown");
        holder.tvSub.setText(getRoleLabel(user.getRole()) + (user.getEmail() != null ? "  •  " + user.getEmail() : ""));

        // Status badge
        String status = user.getStatus();
        if (status == null) status = "unknown";
        holder.tvStatus.setText(capitalize(status));
        applyStatusStyle(holder.tvStatus, status, ctx);

        // Block / Unblock button
        boolean isBlocked = "blocked".equals(status);
        holder.btnBlockUnblock.setText(isBlocked ? "Unblock" : "Block");
        int blockColor = isBlocked
                ? ctx.getResources().getColor(R.color.pg_primary, null)
                : ctx.getResources().getColor(R.color.colorRed, null);
        holder.btnBlockUnblock.setBackgroundTintList(android.content.res.ColorStateList.valueOf(blockColor));

        String finalStatus = status;
        holder.btnBlockUnblock.setOnClickListener(v -> {
            String newStatus = "blocked".equals(finalStatus) ? "active" : "blocked";
            String collection = "rider".equals(user.getRole()) ? "riders" : "users";

            new AlertDialog.Builder(ctx)
                    .setTitle(isBlocked ? "Unblock User?" : "Block User?")
                    .setMessage((isBlocked ? "Allow" : "Prevent") + " " + user.getName() + " from accessing the app?")
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        Map<String, Object> update = new HashMap<>();
                        update.put("status", newStatus);

                        db.collection(collection).document(user.getId())
                                .set(update, SetOptions.merge())
                                .addOnSuccessListener(unused -> {
                                    // Also update users collection
                                    db.collection("users").document(user.getId())
                                            .set(update, SetOptions.merge());
                                    Toast.makeText(ctx, "User " + newStatus, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(ctx, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Edit button
        holder.btnEdit.setOnClickListener(v -> showEditDialog(ctx, user));
    }

    private void showEditDialog(Context ctx, UserModel user) {
        View dialogView = LayoutInflater.from(ctx).inflate(android.R.layout.select_dialog_item, null);

        // Build simple edit dialog
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        EditText etName = new EditText(ctx);
        etName.setHint("Name");
        etName.setText(user.getName());
        layout.addView(etName);

        EditText etPhone = new EditText(ctx);
        etPhone.setHint("Phone");
        etPhone.setText(user.getPhone());
        layout.addView(etPhone);

        new AlertDialog.Builder(ctx)
                .setTitle("Edit User")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newPhone = etPhone.getText().toString().trim();

                    if (newName.isEmpty()) {
                        Toast.makeText(ctx, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", newName);
                    if (!newPhone.isEmpty()) updates.put("phone", newPhone);

                    String collection = "rider".equals(user.getRole()) ? "riders" : "users";
                    db.collection(collection).document(user.getId())
                            .set(updates, SetOptions.merge())
                            .addOnSuccessListener(unused -> {
                                db.collection("users").document(user.getId()).set(updates, SetOptions.merge());
                                Toast.makeText(ctx, "User updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(ctx, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void applyStatusStyle(TextView tv, String status, Context ctx) {
        int textColor, bgRes;
        switch (status.toLowerCase()) {
            case "blocked":
                textColor = ctx.getResources().getColor(R.color.colorRed, null);
                bgRes = R.drawable.bg_warning_red;
                break;
            case "pending":
                textColor = ctx.getResources().getColor(R.color.colorOrange, null);
                bgRes = R.drawable.bg_warning_red;
                break;
            case "approved":
            case "active":
                textColor = ctx.getResources().getColor(R.color.pg_primary, null);
                bgRes = R.drawable.bg_badge_green;
                break;
            default:
                textColor = ctx.getResources().getColor(R.color.pg_sub, null);
                bgRes = R.drawable.bg_badge_green;
        }
        tv.setTextColor(textColor);
        tv.setBackgroundResource(bgRes);
    }

    private String getRoleLabel(String role) {
        if (role == null) return "User";
        switch (role) {
            case "pharmacy_owner": return "Pharmacy";
            case "rider": return "Rider";
            case "customer": return "Customer";
            default: return capitalize(role);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public int getItemCount() { return users.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName, tvSub, tvStatus;
        MaterialButton btnBlockUnblock, btnEdit;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivUserIcon);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvSub = itemView.findViewById(R.id.tvUserSub);
            tvStatus = itemView.findViewById(R.id.tvUserStatus);
            btnBlockUnblock = itemView.findViewById(R.id.btnBlockUnblock);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
        }
    }
}
