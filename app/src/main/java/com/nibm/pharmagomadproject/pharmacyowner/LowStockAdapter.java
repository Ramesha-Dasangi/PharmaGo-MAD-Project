package com.nibm.pharmagomadproject.pharmacyowner;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.nibm.pharmagomadproject.R;


import java.util.ArrayList;



public class LowStockAdapter
        extends RecyclerView.Adapter<LowStockAdapter.ViewHolder>{



    private Context context;

    private ArrayList<InventoryModel> list;



    public LowStockAdapter(Context context,
                           ArrayList<InventoryModel> list){

        this.context=context;
        this.list=list;

    }




    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType){


        View view =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.item_low_stock,
                                parent,
                                false
                        );


        return new ViewHolder(view);

    }





    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position){


        InventoryModel item =
                list.get(position);



        holder.txtName.setText(
                item.getMedicineName()
        );


        holder.txtCategory.setText(
                item.getCategory()
        );



        holder.txtStock.setText(
                "Stock : " + item.getStock()
        );



        holder.txtReorderLevel.setText(
                "Low Stock Limit : " + item.getMaxStock()
        );



        holder.txtPrice.setText(
                "Price : Rs."+
                        item.getPrice()
        );




        holder.btnReorder.setOnClickListener(v -> {


            Toast.makeText(
                    context,
                    "Restock "+item.getMedicineName(),
                    Toast.LENGTH_SHORT
            ).show();



        });



    }




    @Override
    public int getItemCount(){

        return list.size();

    }





    public static class ViewHolder
            extends RecyclerView.ViewHolder{


        TextView txtName;
        TextView txtCategory;
        TextView txtStock;
        TextView txtReorderLevel;
        TextView txtPrice;

        Button btnReorder;



        public ViewHolder(
                @NonNull View itemView){

            super(itemView);



            txtName =
                    itemView.findViewById(R.id.txtName);


            txtCategory =
                    itemView.findViewById(R.id.txtCategory);


            txtStock =
                    itemView.findViewById(R.id.txtStock);


            txtReorderLevel =
                    itemView.findViewById(R.id.txtReorderLevel);



            txtPrice =
                    itemView.findViewById(R.id.txtPrice);



            btnReorder =
                    itemView.findViewById(R.id.btnReorder);


        }


    }


}