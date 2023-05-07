package com.example.outfitowl;

import static com.example.outfitowl.signUp.encodeEmail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class outfits extends AppCompatActivity {
    private ImageSlider topImageSlider;
    private ImageSlider bottomImageSlider;
    private ImageSlider outerWearImageSlider;
    private ImageSlider accessoriesImageSlider;
    private ImageSlider othersImageSlider;
    private ImageSlider shoesImageSlider;
    private RecyclerView recyclerView;
    private TextView outfits;
    private recentOutfitsAdapter adapter;
    private ArrayList<outfitData> latestOutfits;
    private FloatingActionButton help;
    private FloatingActionButton share;
    private Button wear;
    private Button clear;
    private EditText outfitName;
    private String userEmail = null;
    private String userName = null;
    private String currentID = null;
    private ArrayList<imageData> wardrobe;
    private ArrayList<String> tops;
    private ArrayList<String> bottoms;
    private ArrayList<String> outerWear;
    private ArrayList<String> accessories;
    private ArrayList<String> others;
    private ArrayList<String> shoes;


    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("wardrobe");
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oufits);

        topImageSlider = findViewById(R.id.topsImageSlider);
        bottomImageSlider = findViewById(R.id.bottomImageSlider);
        outerWearImageSlider = findViewById(R.id.outerWearImageSlider);
        accessoriesImageSlider = findViewById(R.id.accessoriesImageSlider);
        othersImageSlider = findViewById(R.id.othersImageSlider);
        shoesImageSlider = findViewById(R.id.shoesImageSlider);
        clear = findViewById(R.id.clearButton);
        wear = findViewById(R.id.wearButton);
        outfitName = findViewById(R.id.outfitName);
        recyclerView = findViewById(R.id.outfitRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        latestOutfits = new ArrayList<>();
        adapter = new recentOutfitsAdapter(this, latestOutfits);
        recyclerView.setAdapter(adapter);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_outfits);
        outfits = findViewById(R.id.outfits);
        wardrobe = new ArrayList<>();
        tops = new ArrayList<>();
        bottoms = new ArrayList<>();
        outerWear = new ArrayList<>();
        accessories = new ArrayList<>();
        others = new ArrayList<>();
        shoes = new ArrayList<>();


        setUpSlider(topImageSlider);
        setUpSlider(bottomImageSlider);
        setUpSlider(outerWearImageSlider);
        setUpSlider(accessoriesImageSlider);
        setUpSlider(othersImageSlider);
        setUpSlider(shoesImageSlider);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            userEmail =  encodeEmail(currentUser.getEmail());
        }

        if (userEmail != null) {
            getUsername(userEmail, new outfits.OnUsernameRetrievedListener() {
                @Override
                public void onUsernameRetrieved(String username) {
                    userName = username;
                    databaseReference = databaseReference.child(userName);
                    // ValueEventListener for the Firebase Database reference
                    databaseReference.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            wardrobe.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                imageData dataClass = dataSnapshot.getValue(imageData.class);
                                if (dataClass != null) {
                                    wardrobe.add(dataClass);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("TAG", "Error: ", error.toException());
                        }
                    });


                    DatabaseReference outfitsReference = databaseReference.child("outfits");
                    Query latestOutfitsQuery = outfitsReference.orderByChild("timestamp").limitToLast(10);
                    latestOutfitsQuery.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            latestOutfits = new ArrayList<>();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                outfitData outfitData = dataSnapshot.getValue(outfitData.class);
                                if (outfitData != null) {
                                    latestOutfits.add(outfitData);
                                }
                            }
                            Collections.reverse(latestOutfits);
                            adapter.updateData(latestOutfits);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    Intent intent = getIntent();
                    if(intent.getStringExtra("editOutfit")!= null){
                        currentID = intent.getStringExtra("editOutfit");
                        outfitsReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    outfitData dataClass = dataSnapshot.getValue(outfitData.class);
                                    if (dataClass != null && Objects.equals(dataClass.getId(), currentID)) {
                                        Set<String> keys = dataClass.getItems().keySet();
                                        for (String key : keys) {
                                            ArrayList<String> value = dataClass.getItems().get(key);
                                            if(value == null){
                                                continue;
                                            }
                                            ArrayList<SlideModel> slideModels = new ArrayList<>();
                                            for (String imageUrl : value) {
                                                slideModels.add(new SlideModel(imageUrl, ScaleTypes.CENTER_CROP));
                                            }
                                            switch (key) {
                                                case "Tops":
                                                    tops = value;
                                                    topImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                                                    break;
                                                case "Bottoms":
                                                    bottoms = value;
                                                    bottomImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                                                    break;
                                                case "Outerwear":
                                                    outerWear = value;
                                                    outerWearImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                                                    break;
                                                case "Accessories":
                                                    accessories = value;
                                                    accessoriesImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                                                    break;
                                                case "Others":
                                                    others = value;
                                                    othersImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                                                    break;
                                                case "Shoes":
                                                    shoes = value;
                                                    shoesImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                                                    break;
                                            }
                                        }
                                        outfitName.setText(dataClass.getName());
                                        setUpItemSliderOnclickListener();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            });
        }

        adapter.setOnItemClickListener(new recentOutfitsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(HashMap<String, ArrayList<String>> items, String name, String ID) {
                currentID = ID;
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
                            tops = value;
                            topImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                            break;
                        case "Bottoms":
                            bottoms = value;
                            bottomImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                            break;
                        case "Outerwear":
                            outerWear = value;
                            outerWearImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                            break;
                        case "Accessories":
                            accessories = value;
                            accessoriesImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                            break;
                        case "Others":
                            others = value;
                            othersImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                            break;
                        case "Shoes":
                            shoes = value;
                            shoesImageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                            break;
                    }
                }
                outfitName.setText(name);
                setUpItemSliderOnclickListener();
            }
        });

        outfits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(latestOutfits.isEmpty()){
                    Toast.makeText(outfits.this, "Please add outfits", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(outfits.this, allOutfits.class));
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpSlider(topImageSlider);
                setUpSlider(bottomImageSlider);
                setUpSlider(outerWearImageSlider);
                setUpSlider(accessoriesImageSlider);
                setUpSlider(othersImageSlider);
                setUpSlider(shoesImageSlider);

                setUpItemSliderOnclickListener();
            }
        });

        wear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOutfit();
            }
        });

        setUpItemSliderOnclickListener();



        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_wardrobe) {
                startActivity(new Intent(getApplicationContext(), wardrobe.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_outfits) {
                return true;
            } else if (itemId == R.id.bottom_camera) {
                startActivity(new Intent(getApplicationContext(), camera.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_social) {
                startActivity(new Intent(getApplicationContext(), social.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_profile) {
                startActivity(new Intent(getApplicationContext(), profile.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }


    private void setUpSlider(ImageSlider imageSlider){
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.more, ScaleTypes.CENTER_CROP));
        imageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
    }

    private ArrayList<String> getItems(String type, ImageSlider imageSlider){
        AlertDialog.Builder builder = new AlertDialog.Builder(outfits.this);
        View dialogViewOutfitItems = getLayoutInflater().inflate(R.layout.dialog_all_clothes_recycler, null);
        TextView typeTitle = dialogViewOutfitItems.findViewById(R.id.typeTitle);
        ImageView defaultImage = dialogViewOutfitItems.findViewById(R.id.defaultImage);
        TextView defaultText = dialogViewOutfitItems.findViewById(R.id.defaultText);
        ArrayList<SlideModel> typesList = new ArrayList<>();
        ArrayList<String> items = new ArrayList<>();
        builder.setView(dialogViewOutfitItems);
        AlertDialog dialog = builder.create();
        if(dialog.getWindow() != null){
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        RecyclerView recyclerView = dialogViewOutfitItems.findViewById(R.id.itemsRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<imageData> recyclerImageData = new ArrayList<>();
        clothesRecyclerAdapter adapter = new clothesRecyclerAdapter(recyclerImageData, this, true);
        recyclerView.setAdapter(adapter);

        typeTitle.setText(type);

        for (imageData data : wardrobe) {
            String imageType = data.getItemType();
            if (Objects.equals(imageType, type)) {
                recyclerImageData.add(data);
            }
            adapter.notifyDataSetChanged();
            adapter.updateSelectedItems();
        }

        if (recyclerImageData.isEmpty()) {
            defaultText.setVisibility(View.VISIBLE);
            defaultImage.setVisibility(View.VISIBLE);
        } else {
            defaultImage.setVisibility(View.GONE);
            defaultText.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();

        dialog.show();
        adapter.setOnItemClickListener(new clothesRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String itemUrl, int position) {
                boolean isSelected = adapter.getSelectedItems()[position];
                if (isSelected) {
                    typesList.add(new SlideModel(itemUrl, ScaleTypes.CENTER_CROP));
                    items.add(itemUrl);
//                    Log.d("TAG","Selected items array: " + items);
                } else {
                    typesList.removeIf(slideModel -> slideModel.getImageUrl().equals(itemUrl));
                    items.remove(itemUrl);
                }
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (!typesList.isEmpty()) {
                    imageSlider.setImageList(typesList, ScaleTypes.CENTER_CROP);
                }else{
                    setUpSlider(imageSlider);
                }
                imageSlider.setItemClickListener(position -> {
                    switch (type) {
                        case "Tops":
                            tops = getItems(type, imageSlider);
                            break;
                        case "Bottoms":
                            bottoms = getItems(type, imageSlider);
                            break;
                        case "Outerwear":
                            outerWear = getItems(type, imageSlider);
                            break;
                        case "Accessories":
                            accessories = getItems(type, imageSlider);
                            break;
                        case "Others":
                            others = getItems(type, imageSlider);
                            break;
                        case "Shoes":
                            shoes = getItems(type, imageSlider);
                            break;
                    }
                });
            }
        });

        dialog.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return items;
    }

    private void setUpItemSliderOnclickListener(){
        topImageSlider.setItemClickListener(position -> {
            tops.clear();
            tops = getItems("Tops", topImageSlider);
        });

        bottomImageSlider.setItemClickListener(position -> {
            bottoms.clear();
            bottoms = getItems("Bottoms", bottomImageSlider);
        });

        outerWearImageSlider.setItemClickListener(position -> {
            bottoms.clear();
            outerWear = getItems("Outerwear", outerWearImageSlider);
        });

        accessoriesImageSlider.setItemClickListener(position -> {
            bottoms.clear();
            accessories = getItems("Accessories", accessoriesImageSlider);
        });

        othersImageSlider.setItemClickListener(position -> {
            bottoms.clear();
            others = getItems("Others", othersImageSlider);
        });

        shoesImageSlider.setItemClickListener(position -> {
            bottoms.clear();
            shoes = getItems("Shoes", shoesImageSlider);
        });
    }


    private void saveOutfit() {
        HashMap<String, ArrayList<String>> selectedItems = new HashMap<>();
        if(tops.isEmpty()){
            Toast.makeText(this, "Please select at least one top for the outfit.", Toast.LENGTH_SHORT).show();
            return;
        } else if (bottoms.isEmpty()) {
            Toast.makeText(this, "Please select at least one bottom for the outfit.", Toast.LENGTH_SHORT).show();
            return;
        } else if (shoes.isEmpty()) {
            Toast.makeText(this, "Please select at least one bottom for the outfit.", Toast.LENGTH_SHORT).show();
            return;
        }
        selectedItems.put("Tops", tops);
        selectedItems.put("Bottoms", bottoms);
        selectedItems.put("Outerwear", outerWear);
        selectedItems.put("Accessories", accessories);
        selectedItems.put("Others", others);
        selectedItems.put("Shoes", shoes);


        String name = outfitName.getText().toString();;
        if(name.equals("")){
            Toast.makeText(this, "Please enter a name for your outfit.", Toast.LENGTH_SHORT).show();
            return;
        }
        long timestamp = System.currentTimeMillis();
        String id;
        if(currentID != null){
            id = currentID;
        }else{
            id = databaseReference.child("outfits").push().getKey();
        }

        if (id != null) {
            outfitData newOutfit = new outfitData(id, name, selectedItems, timestamp);
            databaseReference.child("outfits").child(id).setValue(newOutfit)
                    .addOnSuccessListener(aVoid -> Toast.makeText(outfits.this, "Outfit saved successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(outfits.this, "Error saving outfit!", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(outfits.this, "Error saving outfit!", Toast.LENGTH_SHORT).show();
        }
        currentID = null;
    }

    public interface OnUsernameRetrievedListener {
        void onUsernameRetrieved(String username);
    }

    private void getUsername(String email, OnUsernameRetrievedListener listener) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("user");
        Query query = usersRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.child("username").getValue(String.class);
                    listener.onUsernameRetrieved(username);
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "Error: ", error.toException());
            }
        });
    }

}