package com.nibm.pharmagomadproject.pharmacyowner;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.nibm.pharmagomadproject.R;
import com.nibm.pharmagomadproject.pharmacyowner.inventory.EditMedicineActivity;


import java.util.ArrayList;



public class InventoryAdapter
        extends RecyclerView.Adapter<InventoryAdapter.ViewHolder>{



    private Context context;

    private ArrayList<InventoryModel> inventoryList;



    public InventoryAdapter(Context context,
                            ArrayList<InventoryModel> inventoryList){

        this.context=context;
        this.inventoryList=inventoryList;

    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType){


        View view = LayoutInflater.from(context)
                .inflate(R.layout.inventory_item,parent,false);


        return new ViewHolder(view);

    }





    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position){



        InventoryModel item =
                inventoryList.get(position);



        holder.txtMedicine.setText(
                item.getMedicineName()
        );



        holder.txtCategory.setText(
                item.getCategory()
        );



        holder.txtPrice.setText(
                "Rs." + String.format("%.2f",
                        item.getPrice())
        );





        holder.progressStock.setMax(
                item.getMaxStock()
        );


        holder.progressStock.setProgress(
                item.getStock()
        );





        if(item.getStock()==0){


            holder.txtStock.setText(
                    "Out of Stock"
            );


            holder.txtStock.setTextColor(
                    Color.RED
            );


        }
        else if(item.getStock()<20){



            holder.txtStock.setText(
                    item.getStock()+" units - Low Stock"
            );


            holder.txtStock.setTextColor(
                    Color.RED
            );



        }
        else{


            holder.txtStock.setText(
                    item.getStock()+" units"
            );


            holder.txtStock.setTextColor(
                    Color.GREEN
            );


        }





        holder.imgEdit.setOnClickListener(v->{



            Intent intent =
                    new Intent(
                            context,
                            EditMedicineActivity.class
                    );



            intent.putExtra(
                    "medicineName",
                    item.getMedicineName()
            );


            context.startActivity(intent);



        });



    }







    @Override
    public int getItemCount(){

        return inventoryList.size();

    }






    public void updateList(
            ArrayList<InventoryModel> newList){


        inventoryList =
                new ArrayList<>(newList);


        notifyDataSetChanged();


    }







    public static class ViewHolder
            extends RecyclerView.ViewHolder{


        TextView txtMedicine;
        TextView txtCategory;
        TextView txtPrice;
        TextView txtStock;

        ProgressBar progressStock;

        ImageView imgEdit;



        public ViewHolder(
                @NonNull View itemView){

            super(itemView);



            txtMedicine =
                    itemView.findViewById(R.id.txtMedicine);


            txtCategory =
                    itemView.findViewById(R.id.txtCategory);


            txtPrice =
                    itemView.findViewById(R.id.txtPrice);


            txtStock =
                    itemView.findViewById(R.id.txtStock);



            progressStock =
                    itemView.findViewById(R.id.progressStock);


            imgEdit =
                    itemView.findViewById(R.id.imgEdit);


        }


    }


}