package com.nibm.pharmagomadproject.customer.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
                holder.name.setText(m.getBrandName() + " " + m.getMedicineName());
            } else {
                holder.name.setText(m.getMedicineName());
            }
        }

        // Category subtitle
        if (holder.subtitle != null) {
            String cat  = m.getCategory()  != null ? m.getCategory()  : "";
            String phm  = m.getPharmacy()  != null ? m.getPharmacy()  : "";
            holder.subtitle.setText(cat.isEmpty() ? phm : cat + " · " + phm);
        }

        // Price + best price badge
        boolean isBest = bestPriceMap.containsKey(m.getMedicineName())
                && m.getPrice() == bestPriceMap.get(m.getMedicineName())
                && medicineList.stream().filter(
                med -> med.getMedicineName().equals(m.getMedicineName())
        ).count() > 1;

        if (holder.price != null) {
            if (isBest) {
                holder.price.setText("Rs. " + (int) m.getPrice() + "  ✓ Best");
                holder.price.setTextColor(context.getResources()
                        .getColor(R.color.pg_primary, null));
                holder.price.setTypeface(null, Typeface.BOLD);
            } else {
                holder.price.setText("Rs. " + (int) m.getPrice());
                holder.price.setTextColor(context.getResources()
                        .getColor(R.color.pg_primary, null));
                holder.price.setTypeface(null, Typeface.NORMAL);
            }
        }

        // RX Badge — show for Prescription/Rx type or category
        if (holder.rxBadge != null) {
            boolean isRx = "Prescription".equalsIgnoreCase(m.getType())
                    || "Rx".equalsIgnoreCase(m.getType())
                    || "Prescription".equalsIgnoreCase(m.getCategory())
                    || "Rx".equalsIgnoreCase(m.getCategory());
            holder.rxBadge.setVisibility(isRx ? View.VISIBLE : View.GONE);
        }

        // Load medicine image with Glide (show real image, hide pill icon; fallback to icon)
        String imageUrl = m.getImageUrl();
        if (holder.medicineImage != null && holder.medicineIcon != null) {
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                holder.medicineIcon.setVisibility(View.GONE);
                holder.medicineImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(imageUrl.trim())
                        .placeholder(R.drawable.ic_pill)
                        .error(R.drawable.ic_pill)
                        .centerCrop()
                        .into(holder.medicineImage);
            } else {
                holder.medicineImage.setVisibility(View.GONE);
                holder.medicineIcon.setVisibility(View.VISIBLE);
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
        TextView  brand, name, price, subtitle, rxBadge;
        ImageView medicineImage, medicineIcon;
        View      addCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            brand         = itemView.findViewById(R.id.tvBrand);
            name          = itemView.findViewById(R.id.tvMedicineName);
            price         = itemView.findViewById(R.id.tvMedicinePrice);
            subtitle      = itemView.findViewById(R.id.tvMedicineCategory);
            rxBadge       = itemView.findViewById(R.id.tvRxBadge);
            medicineImage = itemView.findViewById(R.id.ivMedicineImage);
            medicineIcon  = itemView.findViewById(R.id.ivMedicineIcon);
            addCart       = itemView.findViewById(R.id.btnAddToCartCard);
        }
    }
}