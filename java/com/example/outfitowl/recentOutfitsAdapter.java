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
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class recentOutfitsAdapter extends RecyclerView.Adapter<recentOutfitsAdapter.MyViewHolder> {
    private ArrayList<outfitData> dataList;
    private Context context;
    private recentOutfitsAdapter.OnItemClickListener onItemClickListener;
    public recentOutfitsAdapter(Context context, ArrayList<outfitData> dataList) {
        this.context = context;
        this.dataList = dataList;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_outfits_recycler_items, parent, false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Set<String> keys = dataList.get(position).getItems().keySet();
        for (String key : keys) {
            if (Objects.equals(key, "Tops")){
                String topUrl = dataList.get(position).getItems().get(key).get(0);
                Glide.with(context).load(topUrl).into(holder.recyclerImageTop);
            } else if (Objects.equals(key, "Bottoms")) {
                String bottomUrl = dataList.get(position).getItems().get(key).get(0);
                Glide.with(context).load(bottomUrl).into(holder.recyclerImageBottom);
            }else if(Objects.equals(key, "Shoes")) {
                String shoesUrl = dataList.get(position).getItems().get(key).get(0);
                Glide.with(context).load(shoesUrl).into(holder.recyclerImageShoes);
            }
        }
        holder.recyclerCaption.setText(dataList.get(position).getName());
    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateData(ArrayList<outfitData> newDataList) {
        this.dataList = newDataList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(HashMap<String, ArrayList<String>> items, String name, String key);
    }

    public void setOnItemClickListener(recentOutfitsAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView recyclerImageTop;
        ImageView recyclerImageBottom;
        ImageView recyclerImageShoes;
        TextView recyclerCaption;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerImageTop = itemView.findViewById(R.id.recyclerImageTop);
            recyclerImageBottom = itemView.findViewById(R.id.recyclerImageBottom);
            recyclerImageShoes = itemView.findViewById(R.id.recyclerImageShoes);
            recyclerCaption = itemView.findViewById(R.id.recyclerCaption);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onItemClick(dataList.get(position).getItems(), dataList.get(position).getName(), dataList.get(position).getId());
                        }
                    }
                }
            });
        }
    }
}
