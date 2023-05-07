package com.example.outfitowl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class outfitsRecyclerAdapter extends RecyclerView.Adapter<outfitsRecyclerAdapter.MyViewHolder>{

    private ArrayList<outfitData> dataList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public outfitsRecyclerAdapter(Context context,ArrayList<outfitData> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public outfitsRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_oufits_recycler_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull outfitsRecyclerAdapter.MyViewHolder holder, int position) {
        HashMap<String, ArrayList<String>> items = dataList.get(position).getItems();
        Set<String> keys = items.keySet();

        for (String key : keys) {
            ArrayList<String> value = items.get(key);
            if(value == null){
                continue;
            }
            ArrayList<SlideModel> slideModels = new ArrayList<>();
            for (String imageUrl : value) {
                slideModels.add(new SlideModel(imageUrl, ScaleTypes.CENTER_CROP));
            }
            switch (key) {
                case "Tops":
                    holder.topsImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                    break;
                case "Bottoms":
                    holder.bottomImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                    break;
                case "Outerwear":
                    holder.outerWearImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                    break;
                case "Accessories":
                    holder.accessoriesImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                    break;
                case "Others":
                    holder.othersImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                    break;
                case "Shoes":
                    holder.shoesImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                    break;
            }
        }
        holder.outfitName.setText(dataList.get(position).getName());
        holder.deleteOutfitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                onItemClickListener.onDeleteButtonClick(dataList.get(currentPosition));
            }
        });
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = holder.getAdapterPosition();
                onItemClickListener.onEditButtonClick(dataList.get(currentPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface OnItemClickListener {
        void onDeleteButtonClick(outfitData outfit);
        void onEditButtonClick(outfitData outfit);

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageSlider topsImageSlider;
        ImageSlider bottomImageSlider;
        ImageSlider accessoriesImageSlider;
        ImageSlider shoesImageSlider;
        ImageSlider outerWearImageSlider;
        ImageSlider othersImageSlider;
        TextView outfitName;
        Button deleteOutfitButton;
        Button editButton;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            topsImageSlider = itemView.findViewById(R.id.topsImageSlider);
            bottomImageSlider = itemView.findViewById(R.id.bottomImageSlider);
            accessoriesImageSlider = itemView.findViewById(R.id.accessoriesImageSlider);
            shoesImageSlider = itemView.findViewById(R.id.shoesImageSlider);
            outerWearImageSlider = itemView.findViewById(R.id.outerWearImageSlider);
            othersImageSlider = itemView.findViewById(R.id.othersImageSlider);
            outfitName = itemView.findViewById(R.id.outfitName);
            deleteOutfitButton = itemView.findViewById(R.id.deleteOutfitButton);
            editButton = itemView.findViewById(R.id.editButton);

        }
    }
}
