package com.example.outfitowl;

import static com.example.outfitowl.signUp.encodeEmail;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class wardrobe extends AppCompatActivity {
    // Declare variables
    private ImageSlider imageSlider;
    private RecyclerView recyclerView;
    private String currentType = null;
    private static final long DOUBLE_CLICK_INTERVAL = 300;
    private FloatingActionButton delete;
    private FloatingActionButton help;
    private long lastClickTime = 0;
    private int currentPosition = 0;
    private TextView currentItemType;
    private TextView wardrobeText;
    private ArrayList<imageData> recyclerImageData;
    private ArrayList<imageData> sliderImageData;
    private ArrayList<imageData> allImageData;
    private clothesRecyclerAdapter adapter;
    private String userEmail = null;
    private String userName = null;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("wardrobe");
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wardrobe);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        currentItemType = findViewById(R.id.itemType);
        imageSlider = findViewById(R.id.imageSlider);
        wardrobeText = findViewById(R.id.wardrobe);
        delete = findViewById(R.id.deleteFab);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerImageData = new ArrayList<>();
        sliderImageData = new ArrayList<>();
        allImageData = new ArrayList<>();
        adapter = new clothesRecyclerAdapter(recyclerImageData, this, false);
        recyclerView.setAdapter(adapter);

        // Get current user's email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            userEmail =  encodeEmail(currentUser.getEmail());
        }
        //get username from user email
        if (userEmail != null) {
            getUsername(userEmail, new wardrobe.OnUsernameRetrievedListener() {
                @Override
                public void onUsernameRetrieved(String username) {
                    userName = username;
                    databaseReference = databaseReference.child(userName);
                    // ValueEventListener for the Firebase Database reference
                    databaseReference.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            recyclerImageData.clear();
                            sliderImageData.clear();
                            allImageData.clear();
                            // To keep track of added image types
                            Set<String> imageTypesAdded = new HashSet<>();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                imageData dataClass = dataSnapshot.getValue(imageData.class);
                                if (dataClass != null && dataClass.getItemType() != null) {
                                    String imageType = dataClass.getItemType();
//                                    dataClass.setItemKey(dataSnapshot.getKey());
                                    allImageData.add(dataClass);
                                    if (!imageTypesAdded.contains(imageType)){
                                        Log.d("TAG","Selected items array: " + imageType);;
                                        recyclerImageData.add(dataClass);
                                        imageTypesAdded.add(imageType);
                                    }
                                    if(Objects.equals(imageType, currentType)){
                                        currentItemType.setText(currentType);
                                        sliderImageData.add(dataClass);
                                    }else if (currentType == null){
                                        currentType = imageType;
                                        currentItemType.setText(imageType);
                                        sliderImageData.add(dataClass);
                                    }
                                }
                            }
                            if(imageTypesAdded.isEmpty()){
                                ArrayList<SlideModel> slideModels = new ArrayList<>();
                                slideModels.add(new SlideModel(R.drawable.please_add,"Please add an Item",ScaleTypes.CENTER_CROP));
                                imageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                            }else {
                                setupImageSlider(sliderImageData);
                            }
                            //using notifyItemInserted() caused an error
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        }

        // Delete button click listener
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(wardrobe.this);
                View dialogViewDelete = getLayoutInflater().inflate(R.layout.dialog_delete, null);
                if (currentPosition >= 0 && currentPosition < sliderImageData.size()) {
                    imageData data = sliderImageData.get(currentPosition);
                    String key = data.getItemKey();
                    if (key != null) {
                        DatabaseReference childReference = databaseReference.child(key);
                        builder.setView(dialogViewDelete);
                        AlertDialog dialog = builder.create();
                        if(dialog.getWindow() != null){
                            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }
                        dialog.show();
                        dialog.findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                childReference.removeValue(); // Delete the child node using the key
                                Toast.makeText(wardrobe.this, "Item deleted", Toast.LENGTH_SHORT).show();
                                if(sliderImageData.isEmpty()){
                                    ArrayList<SlideModel> slideModels = new ArrayList<>();
                                    slideModels.add(new SlideModel(R.drawable.please_add,"Please add an Item",ScaleTypes.CENTER_CROP));
                                    imageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                                }
                                dialog.dismiss();
                                currentPosition = 0;
                            }
                        });
                        dialogViewDelete.findViewById(R.id.deleteCancelButton).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }else {
                        Toast.makeText(wardrobe.this, "Error: Unable to delete item", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // recycler item click listener
        adapter.setOnItemClickListener(new clothesRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String itemType, int position) {
                currentType = itemType;
                updateSliderImages(currentType);
            }
        });

        wardrobeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerImageData.isEmpty()){
                    Toast.makeText(wardrobe.this, "Please add clothing items", Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(wardrobe.this);
                View dialogViewOutfitItems = getLayoutInflater().inflate(R.layout.dialog_all_clothes_recycler, null);
                ImageView defaultImage = dialogViewOutfitItems.findViewById(R.id.defaultImage);
                TextView defaultText = dialogViewOutfitItems.findViewById(R.id.defaultText);
                builder.setView(dialogViewOutfitItems);
                AlertDialog dialog = builder.create();
                if(dialog.getWindow() != null){
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }

                RecyclerView recyclerView = dialogViewOutfitItems.findViewById(R.id.itemsRecyclerView);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(wardrobe.this));
                ArrayList<imageData> recyclerImageData = new ArrayList<>();
                clothesRecyclerAdapter adapter = new clothesRecyclerAdapter(recyclerImageData, wardrobe.this, true);
                recyclerView.setAdapter(adapter);
                recyclerImageData.addAll(allImageData);
                if (recyclerImageData.isEmpty()) {
                    defaultText.setVisibility(View.VISIBLE);
                    defaultImage.setVisibility(View.VISIBLE);
                } else {
                    defaultImage.setVisibility(View.GONE);
                    defaultText.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
                dialog.show();
                dialog.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_wardrobe);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_wardrobe) {
                return true;
            } else if (itemId == R.id.bottom_outfits) {
                startActivity(new Intent(getApplicationContext(), outfits.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
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

    // Interface for double-click listener
    public interface DoubleClickListener {
        void onDoubleClick();
    }
    // Set up the image slider with provided image data
    private void setupImageSlider(ArrayList<imageData> imageDataList) {
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        for (imageData data : imageDataList) {
            String url = data.getImageURL();
            String title = data.getItemName();
            slideModels.add(new SlideModel(url, title, ScaleTypes.FIT));
        }
        imageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
        imageSlider.setItemClickListener(position -> {
            handleDoubleClick(() -> {
                startActivity(new Intent(getApplicationContext(), camera.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            });
        });

        imageSlider.setItemChangeListener(position -> {
            currentPosition = position;
        });
    }

    // Update the image slider to display images of the selected type
    private void updateSliderImages(String currentType) {
        sliderImageData.clear();
        currentItemType.setText(currentType);
        for (imageData data : allImageData) {
            String imageType = data.getItemType();
            if (Objects.equals(imageType, currentType)) {
                sliderImageData.add(data);
            }
        }
        setupImageSlider(sliderImageData);
    }

    // Handle double-click events for image slider
    private void handleDoubleClick(DoubleClickListener listener) {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime < DOUBLE_CLICK_INTERVAL) {
            listener.onDoubleClick();
        }
        lastClickTime = currentTime;
    }

    public interface OnUsernameRetrievedListener {
        void onUsernameRetrieved(String username);
    }

    // Get the username of the user with the provided email
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