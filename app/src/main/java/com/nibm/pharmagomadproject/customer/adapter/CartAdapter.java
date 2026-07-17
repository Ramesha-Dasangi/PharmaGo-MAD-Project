package com.nibm.pharmagomadproject.customer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.models.Cart;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface CartListener {
        void onQuantityChanged(int position, int newQty);
        void onRemoveItem(int position);
    }

    private final List<Cart>     cartList;
    private final CartListener   listener;

    public CartAdapter(List<Cart> cartList, CartListener listener) {
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Cart item = cartList.get(position);

        h.tvName.setText(item.getMedicineName());
        h.tvBrand.setText(item.getBrandName() + " · " + item.getPharmacyName());
        h.tvPrice.setText("Rs. " + String.format("%.0f", item.getPrice()) + " each");
        h.tvQty.setText(String.valueOf(item.getQuantity()));
        h.tvSubtotal.setText("Rs. " + String.format("%.0f", item.getSubtotal()));

        h.btnPlus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            listener.onQuantityChanged(pos, cartList.get(pos).getQuantity() + 1);
        });

        h.btnMinus.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            int qty = cartList.get(pos).getQuantity();
            if (qty > 1) {
                listener.onQuantityChanged(pos, qty - 1);
            } else {
                listener.onRemoveItem(pos);
            }
        });
    }

    @Override
    public int getItemCount() { return cartList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  tvName, tvBrand, tvPrice, tvQty, tvSubtotal;
        ImageView btnPlus, btnMinus;

        ViewHolder(View v) {
            super(v);
            tvName     = v.findViewById(R.id.tvMedicineName);
            tvBrand    = v.findViewById(R.id.tvBrand);
            tvPrice    = v.findViewById(R.id.tvMedicinePrice);
            tvQty      = v.findViewById(R.id.tvQtyMedicare);
            tvSubtotal = v.findViewById(R.id.tvSubtotal);
            btnPlus    = v.findViewById(R.id.btnPlusMedicare);
            btnMinus   = v.findViewById(R.id.btnMinusMedicare);
        }
    }
}
