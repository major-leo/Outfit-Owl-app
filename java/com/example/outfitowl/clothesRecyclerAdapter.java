package com.example.outfitowl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class clothesRecyclerAdapter extends RecyclerView.Adapter<clothesRecyclerAdapter.MyViewHolder> {

    private ArrayList<imageData> dataList;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private boolean displayItemName;
    private boolean[] selectedItems;


    public clothesRecyclerAdapter(ArrayList<imageData> dataList, Context context, boolean displayItemName) {
        this.dataList = dataList;
        this.context = context;
        this.displayItemName = displayItemName;
        this.selectedItems = new boolean[dataList.size()];
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = displayItemName ? LayoutInflater.from(context).inflate(R.layout.outfit_recycler_items, parent,false) :  LayoutInflater.from(context).inflate(R.layout.type_recycler_items, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(dataList.get(position).getImageURL()).into(holder.recyclerImage);
        String caption = displayItemName ? dataList.get(position).getItemName() : dataList.get(position).getItemType();
        holder.recyclerCaption.setText(caption);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String itemType, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateSelectedItems() {
        selectedItems = new boolean[dataList.size()];
    }

    public boolean[] getSelectedItems() {
        return selectedItems;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView recyclerImage;
        TextView recyclerCaption;
        ImageView selected;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            recyclerImage = itemView.findViewById(R.id.recyclerImage);
            recyclerCaption = itemView.findViewById(R.id.recyclerCaption);
            selected = itemView.findViewById(R.id.selected);


            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                                String result = displayItemName ? dataList.get(position).getImageURL() : dataList.get(position).getItemType();
                            if (displayItemName) {
                                if (selectedItems[position]) {
                                    selected.setVisibility(View.GONE);
                                } else {
                                    selected.setVisibility(View.VISIBLE);
                                }
                                selectedItems[position] = !selectedItems[position];
                            }
                            onItemClickListener.onItemClick(result, position);
                        }
                    }
                }
            });
        }
    }
}
