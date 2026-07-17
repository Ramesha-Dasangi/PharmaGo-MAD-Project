package com.nibm.pharmagomadproject.customer.adapter;


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


        holder.rating.setText(
                "⭐ "+pharmacy.getRating()
        );

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


    }




    @Override
    public int getItemCount(){

        return list.size();

    }







    public static class ViewHolder
            extends RecyclerView.ViewHolder{


        TextView name,rating,address,phone;



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


        }


    }


}