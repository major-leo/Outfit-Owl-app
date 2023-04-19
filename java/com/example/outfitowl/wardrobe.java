package com.example.outfitowl;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ActionTypes;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.TouchListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class wardrobe extends AppCompatActivity {

    private ImageSlider imageSlider;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wardrobe);

        imageSlider = findViewById(R.id.imageSlider);
        ArrayList<SlideModel> slideModels = new ArrayList<>();

        slideModels.add(new SlideModel(R.drawable.test1,"test", ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.test2, ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.test3, ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.test4, ScaleTypes.CENTER_CROP));
        slideModels.add(new SlideModel(R.drawable.test5, ScaleTypes.CENTER_CROP));

        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        imageSlider.setItemClickListener(position -> {
            // Your action on item click
            handleDoubleClick(() -> {
                startActivity(new Intent(getApplicationContext(), camera.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            });
        });

        imageSlider.setTouchListener(new TouchListener() {
            @Override
            public void onTouched(@NonNull ActionTypes actionTypes) {
                if (actionTypes == ActionTypes.DOWN) {
                    imageSlider.startSliding(1000);
                } else if (actionTypes == ActionTypes.UP) {
                    imageSlider.stopSliding();
                }
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

    public interface DoubleClickListener {
        void onDoubleClick();
    }

    private static final long DOUBLE_CLICK_INTERVAL = 300; // Time in milliseconds
    private long lastClickTime = 0;

    private void handleDoubleClick(DoubleClickListener listener) {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime - lastClickTime < DOUBLE_CLICK_INTERVAL) {
            listener.onDoubleClick();
        }
        lastClickTime = currentTime;
    }
}