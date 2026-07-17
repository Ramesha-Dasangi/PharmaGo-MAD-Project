package com.nibm.pharmagomadproject.customer.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.models.Medicine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicineAdapter
        extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    public interface OnMedicineClickListener {
        void onCardClick(Medicine medicine);
        void onAddToCartClick(Medicine medicine);
    }

    private List<Medicine>         medicineList;
    private OnMedicineClickListener listener;
    private Context                context;
    private Map<String, Double>    bestPriceMap = new HashMap<>();

    public MedicineAdapter(Context context, List<Medicine> medicineList,
                           OnMedicineClickListener listener) {
        this.context      = context;
        this.medicineList = medicineList;
        this.listener     = listener;
        computeBestPrices();
    }

    /** Call after list changes to recompute best prices */
    public void updateList(List<Medicine> newList) {
        this.medicineList = newList;
        computeBestPrices();
        notifyDataSetChanged();
    }

    private void computeBestPrices() {
        bestPriceMap.clear();
        for (Medicine m : medicineList) {
            String name = m.getMedicineName();
            if (!bestPriceMap.containsKey(name) || m.getPrice() < bestPriceMap.get(name)) {
                bestPriceMap.put(name, m.getPrice());
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine m = medicineList.get(position);

        // Brand name (if exists)
        if (holder.brand != null) {
            holder.brand.setText(m.getBrandName() != null ? m.getBrandName() : "");
        }

        // Medicine name
        if (holder.name != null) {
            if (m.getBrandName() != null && !m.getBrandName().isEmpty() && holder.brand == null) {
                // If there's no separate brand textview, combine brand and name
                holder.name.setText(m.getBrandName() + "\n" + m.getMedicineName());
            } else {
                holder.name.setText(m.getMedicineName());
            }
        }

        // Category + type + pharmacy (subtitle)
        if (holder.subtitle != null) {
            holder.subtitle.setText(
                    m.getCategory() + " · " + m.getType() + "\n" + m.getPharmacy()
            );
        }

        // Price + best price badge
        boolean isBest = bestPriceMap.containsKey(m.getMedicineName())
                && m.getPrice() == bestPriceMap.get(m.getMedicineName())
                && medicineList.stream().filter(
                med -> med.getMedicineName().equals(m.getMedicineName())
        ).count() > 1;

        if (holder.price != null) {
            if (isBest) {
                holder.price.setText("Rs. " + (int) m.getPrice() + " ✓ Best");
                holder.price.setTextColor(context.getResources()
                        .getColor(R.color.pg_primary, null));
                holder.price.setTypeface(null, Typeface.BOLD);
            } else {
                holder.price.setText("Rs. " + (int) m.getPrice());
                holder.price.setTextColor(context.getResources()
                        .getColor(R.color.pg_text, null));
                holder.price.setTypeface(null, Typeface.NORMAL);
            }
        }

        // Prescription badge (if exists)
        if (holder.typeBadge != null) {
            if ("Prescription".equals(m.getType())) {
                holder.typeBadge.setVisibility(View.VISIBLE);
                holder.typeBadge.setText("Rx");
                holder.typeBadge.setBackgroundResource(R.drawable.bg_tag_amber);
            } else {
                holder.typeBadge.setVisibility(View.VISIBLE);
                holder.typeBadge.setText("OTC");
                holder.typeBadge.setBackgroundResource(R.drawable.bg_tag_green);
            }
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCardClick(m);
        });

        if (holder.addCart != null) {
            holder.addCart.setOnClickListener(v -> {
                if (listener != null) listener.onAddToCartClick(m);
            });
        }
    }

    @Override
    public int getItemCount() { return medicineList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView brand, name, price, subtitle, typeBadge;
        View     addCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            brand     = itemView.findViewById(R.id.tvBrand);
            name      = itemView.findViewById(R.id.tvMedicineName);
            price     = itemView.findViewById(R.id.tvMedicinePrice);
            subtitle  = itemView.findViewById(R.id.tvMedicineCategory);
            typeBadge = itemView.findViewById(R.id.tvTypeBadge);
            addCart   = itemView.findViewById(R.id.btnAddToCartCard);
        }
    }
}