package com.nibm.pharmagomadproject.customer.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.customer.models.Medicine;


public class MedicineAdapter
        extends RecyclerView.Adapter<MedicineAdapter.ViewHolder>{


    private List<Medicine> medicineList;


    public MedicineAdapter(List<Medicine> medicineList){
        this.medicineList = medicineList;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType){


        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine,parent,false);

        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position){


        Medicine medicine = medicineList.get(position);


        holder.brand.setText(
                medicine.getBrandName()
        );


        holder.name.setText(
                medicine.getMedicineName()
        );


        holder.price.setText(
                "Rs. "+medicine.getPrice()
        );

    }



    @Override
    public int getItemCount(){
        return medicineList.size();
    }




    public class ViewHolder extends RecyclerView.ViewHolder{


        TextView brand,name,price;
        Button addCart;


        public ViewHolder(@NonNull View itemView){

            super(itemView);


            brand=itemView.findViewById(R.id.tvBrand);
            name=itemView.findViewById(R.id.tvMedicineName);
            price=itemView.findViewById(R.id.tvPrice);

            addCart=itemView.findViewById(R.id.btnAddCart);

        }
    }
}