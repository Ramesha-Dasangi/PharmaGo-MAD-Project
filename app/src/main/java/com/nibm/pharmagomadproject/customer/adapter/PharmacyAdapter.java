package com.nibm.pharmagomadproject.customer.adapter;


import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.models.Pharmacy;


import java.util.List;



public class PharmacyAdapter
        extends RecyclerView.Adapter<PharmacyAdapter.ViewHolder>{



    public interface PharmacyClick{

        void onClick(Pharmacy pharmacy);

    }



    private List<Pharmacy> list;
    private PharmacyClick listener;



    public PharmacyAdapter(List<Pharmacy> list,
                           PharmacyClick listener){

        this.list=list;
        this.listener=listener;

    }




    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType){


        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_pharmacy,
                                parent,
                                false
                        );


        return new ViewHolder(view);

    }





    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position){


        Pharmacy pharmacy=list.get(position);


        holder.name.setText(
                pharmacy.getName()
        );


        if (pharmacy.getRatingCount() > 0 && pharmacy.getRating() > 0) {
            holder.rating.setText(String.format(java.util.Locale.getDefault(), "⭐ %.1f (%d)", pharmacy.getRating(), pharmacy.getRatingCount()));
        } else {
            holder.rating.setText("⭐ New");
        }

        holder.phone.setText(
                pharmacy.getPhone()
        );


        if (pharmacy.getDistanceKm() >= 0) {
            holder.address.setText(pharmacy.getAddress() + " • " + String.format("%.1f km away", pharmacy.getDistanceKm()));
        } else {
            holder.address.setText(pharmacy.getAddress());
        }


        holder.itemView.setOnClickListener(v -> {

            listener.onClick(pharmacy);

        });

        if (holder.btnCall != null) {
            holder.btnCall.setOnClickListener(v -> {
                try {
                    String phoneNum = pharmacy.getPhone();
                    if (phoneNum != null && !phoneNum.trim().isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phoneNum.trim()));
                        holder.itemView.getContext().startActivity(intent);
                    } else {
                        android.widget.Toast.makeText(holder.itemView.getContext(),
                                "Phone number not available", android.widget.Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ignored) {}
            });
        }

    }




    @Override
    public int getItemCount(){

        return list.size();

    }







    public static class ViewHolder
            extends RecyclerView.ViewHolder{


        TextView name,rating,address,phone;
        View btnCall;



        public ViewHolder(@NonNull View itemView){

            super(itemView);


            name=
                    itemView.findViewById(R.id.tvPharmacyName);


            rating=
                    itemView.findViewById(R.id.tvRating);

            phone =
                    itemView.findViewById(R.id.tvPhone);


            address=
                    itemView.findViewById(R.id.tvAddress);

            btnCall =
                    itemView.findViewById(R.id.btnCallPharmacy);

        }


    }


}